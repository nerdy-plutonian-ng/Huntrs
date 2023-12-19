package com.plutoapps.huntrs

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plutoapps.huntrs.data.models.Hunt
import com.plutoapps.huntrs.data.models.HuntWithCheckpoints
import com.plutoapps.huntrs.data.repos.HuntsRoomRepo
import com.plutoapps.huntrs.ui.routes.Routes
import com.plutoapps.huntrs.ui.screens.home.BluetoothSharingViewModel
import com.plutoapps.huntrs.ui.screens.home.BluetoothSharingViewModelFactory
import com.plutoapps.huntrs.ui.screens.home.HomeScreen
import com.plutoapps.huntrs.ui.screens.home.HuntsViewModel
import com.plutoapps.huntrs.ui.screens.home.HuntsViewModelFactory
import com.plutoapps.huntrs.ui.theme.AppTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val factory = BluetoothSharingViewModelFactory(this)
    private lateinit var sharingViewModel:  BluetoothSharingViewModel

    private lateinit var receiver : BroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharingViewModel =  ViewModelProvider(this,factory)[BluetoothSharingViewModel::class.java]
        receiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    when (intent.action) {
                        BluetoothDevice.ACTION_FOUND -> {
                            // Discovery has found a device. Get the BluetoothDevice
                            // object and its info from the Intent.
                            val device: BluetoothDevice? =
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                            if(device?.name != null && device.address != null) {
                                sharingViewModel.addBtDevice(device)
                            }
                        }
                    }
                }
            }
        }
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
        setContent {
            AppTheme {
                HuntrsApp(sharingViewModel = sharingViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1212 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission has been granted
            // You can proceed with showing notifications here
        } else {
            // Permission has been denied
            // Handle the case where the user denied the permission
            Toast.makeText(this,"Precise location is needed for getting where you add checkpoints",Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("beesh","a return $requestCode $resultCode ")
        if (requestCode == 10101010) {
            if(resultCode == 300){
                Log.d("beesh","hosting connection")
                sharingViewModel.hostConnection()
            }
        }
    }
}

@Composable
fun HuntrsApp(sharingViewModel: BluetoothSharingViewModel?) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val db = HuntrsApplication.db
    val repo = HuntsRoomRepo(db!!.getHuntsDao(), db.getCheckpointDao())
    val factory = HuntsViewModelFactory(repository = repo)
    val viewModel = ViewModelProvider(
        LocalContext.current as ViewModelStoreOwner,
        factory
    )[HuntsViewModel::class.java]

    var isLoadingMine by rememberSaveable {
        mutableStateOf(true)
    }

    var state = viewModel.getHuntsByType(isLoadingMine).collectAsState(initial = emptyList())

    val loadHuntsByType : (Boolean) -> Unit = {
        isLoadingMine = it
    }

    val upsertHunt: (HuntWithCheckpoints) -> Unit = {
        scope.launch {
            viewModel.upsertHunt(it)
        }
    }

    val getHunt : suspend (String) -> HuntWithCheckpoints = {
         viewModel.getHuntById(it)
    }

    val deleteHunt : (Hunt) -> Unit = {
        scope.launch {
            viewModel.deleteHunt(it)
        }
    }
            NavHost(
        navController = navController,
        startDestination = Routes.Home.name
    ) {
        composable(route = Routes.Home.name) {
            HomeScreen(hunts = state.value, upsertHunt = upsertHunt,getHunt = getHunt,deleteHunt = deleteHunt,loadHuntsByType = loadHuntsByType,
                sharingViewModel=  sharingViewModel)
        }
    }
}

@Preview
@Composable
fun HuntrsAppPreview() {
    HuntrsApp(null)
}