package com.plutoapps.huntrs.ui.composables

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plutoapps.huntrs.R
import com.plutoapps.huntrs.data.models.CheckPoint
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.plutoapps.huntrs.data.models.HuntWithCheckpoints
import com.plutoapps.huntrs.data.repos.PermissionsRepo
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID


@SuppressLint("MissingPermission")
@Composable
fun HuntSheet(
    modifier: Modifier = Modifier,
    id: String?,
    dismissSheet: () -> Unit,
    upsertHunt: (HuntWithCheckpoints) -> Unit,
    getHunt: suspend (String) -> HuntWithCheckpoints
) {

    val settingResultRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == RESULT_OK)
            Log.d("appDebug", "Accepted")
        else {
            Log.d("appDebug", "Denied")
        }
    }

    var hunt by rememberSaveable {
        mutableStateOf<HuntWithCheckpoints?>(null)
    }

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val huntId by rememberSaveable {
        mutableStateOf(id ?: UUID.randomUUID().toString())
    }


    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(
            context
        )
    }

    var title by rememberSaveable {
        mutableStateOf("")
    }

    var description by rememberSaveable {
        mutableStateOf("")
    }

    var time by rememberSaveable {
        mutableLongStateOf(Date().time)
    }

    var checkpoints by rememberSaveable {
        mutableStateOf(listOf(CheckPoint(parentId = huntId)))
    }

    var hasLocationPermission by rememberSaveable {
        mutableStateOf(false)
    }

    var isLocationEnabled by rememberSaveable {
        mutableStateOf(false)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        if (!it) {
            Toast.makeText(
                context,
                context.getString(R.string.location_rationale),
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)

        }

    }

    val updateWithLocation: (Int, CheckPoint, Double, Double) -> Unit =
        { index, checkpoint, latitude, longitude ->
            val newCheckpoint = checkpoint.copy(latitude = latitude, longitude = longitude)
            val newCheckpoints = checkpoints.toMutableList()
            newCheckpoints[index] = newCheckpoint
            checkpoints = newCheckpoints.toList()
        }

    val getLocation: (Int, CheckPoint) -> Unit = { index, checkpoint ->

        if(PermissionsRepo(context,context as Activity).canAccessLocation()){
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener {
                    if (it != null) {
                        updateWithLocation(index, checkpoint, it.latitude, it.longitude)
                    }
                }
        }

 /*       if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if(isGpsEnabled) {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener {
                        if (it != null) {
                            updateWithLocation(index, checkpoint, it.latitude, it.longitude)
                        }
                    }
            } else {
                checkLocationSetting(
                    context = context,
                    onDisabled = { intentSenderRequest ->
                        settingResultRequest.launch(intentSenderRequest)
                    },
                    onEnabled = { *//* This will call when setting is already enabled *//* }
                )
            }
        } else if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }*/

    }

    LaunchedEffect(key1 = "123", block = {
        if (id != null) {
            scope.launch {
                hunt = getHunt(id)
                title = hunt?.title ?: ""
                description = hunt?.description ?: ""
                checkpoints = hunt?.checkPoints ?: emptyList()
                time = hunt?.time ?: Date().time

            }
        }
    })

    Column(
        modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (id == null) "New Hunt" else "Update Hunt",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = modifier.height(8.dp))
        Text(
            text = stringResource(R.string.description),
            style = MaterialTheme.typography.titleMedium
        )
        TextBox(text = title, onchange = { title = it }, label = stringResource(R.string.title))
        TextBox(
            text = description,
            onchange = { description = it },
            label = stringResource(R.string.description)
        )
        Spacer(modifier = modifier.height(8.dp))
        Text(
            text = stringResource(R.string.checkpoints),
            style = MaterialTheme.typography.titleMedium
        )
        for (checkpoint in checkpoints) {
            val index = checkpoints.indexOf(checkpoint)

            Column {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextBox(
                        text = checkpoint.title, onchange = {
                            val mutableCheckpoints = checkpoints.toMutableList()
                            val editedCheckpoint = checkpoint.copy(title = it)
                            mutableCheckpoints[index] = editedCheckpoint
                            checkpoints = mutableCheckpoints.toList()
                        },
                        modifier = modifier.weight(1f),
                        label = stringResource(R.string.next_checkpoint_clue),
                        placeholder = { Text(text = stringResource(R.string.clue_to_this_location)) },
                        trailing = {
                            IconButton(onClick = {
                                getLocation(index, checkpoint)
                            }) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    null,
                                    tint = if (checkpoint.latitude != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                                )
                            }
                        }
                    )
                    if (index != 0)
                        IconButton(onClick = {
                            val newCheckpoints = checkpoints.toMutableList()
                            newCheckpoints.removeAt(index)
                            checkpoints = newCheckpoints.toList()
                        }) {
                            Icon(painterResource(id = R.drawable.baseline_remove_circle_24), null)
                        }
                }

                Text(
                    text = if (checkpoint.latitude == null) "Click on the location icon to get coordinates" else "lat : ${checkpoint.latitude}, long : ${checkpoint.longitude}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = modifier.padding(start = 16.dp)
                )
            }
        }
        TextButton(onClick = {
            val mutableCheckpoints = checkpoints.toMutableList()
            val newCheckpoint = CheckPoint(parentId = huntId)
            mutableCheckpoints.add(newCheckpoint)
            checkpoints = mutableCheckpoints.toList()
        }) {
            Text(text = stringResource(R.string.add_another_clue))
        }
        Row {
            Button(onClick = {
                if (checkpoints.none { it.title.isEmpty() || it.latitude == null || it.longitude == null } && title.isNotEmpty()) {
                    val newHunt = HuntWithCheckpoints(
                        id = huntId,
                        title = title,
                        description = description,
                        checkPoints = checkpoints,
                        time = time,
                    )
                    Log.d("beesh about to save", newHunt.toString())
                    upsertHunt(newHunt)
                    dismissSheet()
                } else {
                    Log.d("beesh", "")
                }
            }) {
                Text(text = stringResource(id = if (id == null) R.string.save else R.string.update))
            }
            Spacer(modifier = modifier.width(8.dp))
            OutlinedButton(
                onClick =
                dismissSheet
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
        Spacer(modifier = modifier.height(16.dp))
    }


}

@Preview(showBackground = true)
@Composable
fun HuntSheetPreview() {
    HuntSheet(id = "null", dismissSheet = { }, upsertHunt = {}, getHunt = { HuntWithCheckpoints() })
}

fun checkLocationSetting(
    context: Context,
    onDisabled: (IntentSenderRequest) -> Unit,
    onEnabled: () -> Unit
) {

    val locationRequest = LocationRequest.create().apply {
        interval = 1000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    val client: SettingsClient = LocationServices.getSettingsClient(context)
    val builder: LocationSettingsRequest.Builder = LocationSettingsRequest
        .Builder()
        .addLocationRequest(locationRequest)

    val gpsSettingTask: Task<LocationSettingsResponse> =
        client.checkLocationSettings(builder.build())

    gpsSettingTask.addOnSuccessListener { onEnabled() }
    gpsSettingTask.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                val intentSenderRequest = IntentSenderRequest
                    .Builder(exception.resolution)
                    .build()
                onDisabled(intentSenderRequest)
            } catch (sendEx: IntentSender.SendIntentException) {
                // ignore here
            }
        }
    }

}