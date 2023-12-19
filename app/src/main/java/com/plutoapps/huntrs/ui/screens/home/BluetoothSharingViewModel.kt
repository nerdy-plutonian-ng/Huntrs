package com.plutoapps.huntrs.ui.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.plutoapps.huntrs.data.models.HuntWithCheckpoints
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothSharingViewModel(private val context: Context) : ViewModel() {

    companion object {
        const val NAME = "Huntrs"
        const val APP_ID = "71735293-5629-4f7a-aefb-be85b286aeb8"
        var bluetoothAdapter : BluetoothAdapter? = null
        const val TAG = "Huntrs BSVM"
        var saveHuntFunction : ((HuntWithCheckpoints)->Unit)? = null
    }

    init {
        val bluetoothManager = ContextCompat.getSystemService(context, BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter
    }

    private var _sharingState = MutableStateFlow(SharingState())
    val sharingState: StateFlow<SharingState> = _sharingState

    fun addBtDevice(bluetoothDevice: BluetoothDevice){
        _sharingState.update {
            it.copy(foundDevices = listOf(*it.foundDevices.toSet().toTypedArray(), bluetoothDevice))
        }
    }

    fun setSaveHunt(function : ((HuntWithCheckpoints)->Unit)?){
        saveHuntFunction = function
    }

    fun makeDiscoverable(){
        val requestCode = 10101010
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        (context as Activity).startActivityForResult(discoverableIntent, requestCode)
    }

    fun hostConnection(){
        AcceptConnectionThread().start()
    }

    fun connectToClient(bluetoothDevice: BluetoothDevice,sharedHunt: HuntWithCheckpoints?){
        _sharingState.update {
            it.copy(selectedDevices = bluetoothDevice, stage = SharingStage.Sharing)
        }
        InitiateConnectionThread(bluetoothDevice,sharedHunt).start()
    }

    private fun mopUp(isHost:Boolean){
        _sharingState.update {
            it.copy(sharedHunt = null, stage = SharingStage.Discovering, selectedDevices = null)
        }
    }

    fun selectHuntToShare(huntWithCheckpoints: HuntWithCheckpoints?){
        _sharingState.update {
            it.copy(sharedHunt = huntWithCheckpoints)
        }
    }
    fun discoverBluetoothDevices() {
        if(ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ){
            try {
                // Register for broadcasts when a device is discovered.
                if (bluetoothAdapter != null) {
//                    val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
//                    pairedDevices?.forEach { device ->
//                        if (device.address != null) {
//                            _sharingState.update {
//                                it.copy(foundDevices = listOf(*it.foundDevices.toSet().toTypedArray(), device))
//                            }
//                        }
//                    }
                    viewModelScope.launch {
                        delay(1000)
                        Log.d("beesh","starting discovery")
                        bluetoothAdapter?.startDiscovery()
                    }
                }
            } catch (e:Exception){
                Log.d("beesh",e.toString())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private inner class AcceptConnectionThread : Thread() {

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(NAME, UUID.fromString(APP_ID))
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    manageMyConnectedSocket(it,null)
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    private fun manageMyConnectedSocket(bluetoothSocket: BluetoothSocket,huntWithCheckpoints : HuntWithCheckpoints?) {
        ConnectedThread(bluetoothSocket,huntWithCheckpoints,upsertHunt = saveHuntFunction).start()
    }

    @SuppressLint("MissingPermission")
    private inner class InitiateConnectionThread(device: BluetoothDevice,private val huntWithCheckpoints : HuntWithCheckpoints?) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.fromString(APP_ID))
        }

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(socket,huntWithCheckpoints)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket,private val huntWithCheckpoints : HuntWithCheckpoints?,val upsertHunt :((HuntWithCheckpoints)->Unit)?) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            if(huntWithCheckpoints == null){
                read()
            } else {
                write(huntWithCheckpoints.toJson().toByteArray())
            }
        }

        private fun read(){
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }
                val huntStr = String(mmBuffer,0,numBytes)
                Log.d("beesh what i received",huntStr)
                val hunt = HuntWithCheckpoints.fromJson(huntStr).copy(isMine = false)
                Log.d("beesh what i converted",hunt.toString())
                upsertHunt?.let { it(hunt) }

                // Send the obtained bytes to the UI activity.
                _sharingState.update {
                    it.copy(sharedHunt = HuntWithCheckpoints.fromJson(huntStr))
                }
            }
        }


        // Call this from the main activity to send data to the remote device.
        private fun write(bytes: ByteArray) {
            Log.d("beesh what i sent",huntWithCheckpoints.toString())
            try {
                mmOutStream.write(bytes)
                _sharingState.update {
                    it.copy(stage = SharingStage.Shared)
                }
            } catch (e: IOException) {
                _sharingState.update {
                    it.copy(stage = SharingStage.Discovering)
                }
                return
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }
}

data class SharingState(
    val stage: SharingStage = SharingStage.Discovering,
    val foundDevices: List<BluetoothDevice> = emptyList(),
    val selectedDevices : BluetoothDevice? = null,
    val sharedHunt : HuntWithCheckpoints? = null
)

enum class SharingStage {
    Discovering,
    Sharing,
    Shared
}

class BluetoothSharingViewModelFactory(private val context: Context,) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluetoothSharingViewModel::class.java)) {
            return BluetoothSharingViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}