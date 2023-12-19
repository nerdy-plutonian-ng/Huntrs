package com.plutoapps.huntrs.data.repos

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task


class PermissionsRepo(val context: Context,val activity: Activity) {

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    fun canAccessLocation() : Boolean {
       val locationPermissionStatus =  context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        return if(locationPermissionStatus == PackageManager.PERMISSION_GRANTED){
            if(isLocationEnabled(context)){
                true
            } else {
                promptToEnableLocation(context)
                false
            }
        } else {
            activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,),1212)
            false
        }
    }

    fun canAccessBluetooth() : Boolean {
        val bluetoothPermissionStatus =  context.checkSelfPermission(if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S)Manifest.permission.BLUETOOTH_ADMIN  else Manifest.permission.BLUETOOTH_CONNECT)
        return if(bluetoothPermissionStatus == PackageManager.PERMISSION_GRANTED){
            val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
            if (bluetoothAdapter == null) {
                return false
            } else {
                if(bluetoothAdapter.isEnabled){
                    return true
                } else {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    activity.startActivityForResult(enableBtIntent, 900)
                    return false
                }
            }

        } else {
            activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,),1212)
            false
        }
    }


    private fun promptToEnableLocation(context: Context) {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(context)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // Location services are already enabled
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Prompt the user to enable location services
                    exception.startResolutionForResult(context as Activity, 2121223)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Error handling
                }
            }
        }
    }

}