package com.plutoapps.huntrs.ui.composables

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plutoapps.huntrs.data.models.HuntWithCheckpoints
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.plutoapps.huntrs.R
import com.plutoapps.huntrs.data.repos.PermissionsRepo
import com.plutoapps.huntrs.ui.screens.home.BluetoothSharingViewModel
import com.plutoapps.huntrs.ui.screens.home.BluetoothSharingViewModelFactory
import com.plutoapps.huntrs.ui.screens.home.SharingStage

@SuppressLint("MissingPermission")
@Composable
fun ShareSheet(
    modifier: Modifier = Modifier,
    huntWithCheckpoints: HuntWithCheckpoints?,
    dismissSheet: () -> Unit,
    sharingViewModel: BluetoothSharingViewModel?,
    isSending : Boolean = true
) {

    val context = LocalContext.current

    val state = sharingViewModel!!.sharingState.collectAsState()

    LaunchedEffect(key1 = Key(101), block = {
        Log.d("beesh","is sending = $isSending")
        if(PermissionsRepo(context,context as Activity).canAccessLocation()){
            if(PermissionsRepo(context,context).canAccessBluetooth()){
                if(isSending){
                    sharingViewModel.selectHuntToShare(huntWithCheckpoints)
                    sharingViewModel.discoverBluetoothDevices()
                } else {
                    sharingViewModel.makeDiscoverable()
                }
            }
        }
    })

    if(isSending){
        if(state.value.stage != SharingStage.Discovering){
            SharedView(stage = state.value.stage, isSending = isSending)
        } else {
            Column(
                modifier = modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Share hunt", style = MaterialTheme.typography.titleLarge)
                    if (sharingViewModel.sharingState.collectAsState().value.stage == SharingStage.Discovering)
                        CircularProgressIndicator(modifier = modifier.size(32.dp))
                }
                Spacer(modifier = modifier.height(16.dp))
                for (device in state.value.foundDevices ?: emptyList())
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.clickable {
                        sharingViewModel.selectHuntToShare(huntWithCheckpoints)
                        sharingViewModel.connectToClient(device,huntWithCheckpoints)
                    }) {
                        Icon(painterResource(id = R.drawable.baseline_bluetooth_24), null)
                        Spacer(modifier = modifier.width(4.dp))
                        Column(modifier = modifier.weight(1f)) {
                            Text(device.name, style = MaterialTheme.typography.titleMedium)
                            Text(device.address!!)
                        }
                    }
            }
        }
    }
    else {
        Column(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SharedView(stage = state.value.stage,isSending = isSending)
        }
    }
}

@Composable
fun SharedView(modifier : Modifier = Modifier ,stage: SharingStage, isSending: Boolean) {
    if(stage == SharingStage.Sharing){
        Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if(isSending)"Sharing" else "Receiving", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = modifier.height(8.dp))
            LinearProgressIndicator()
            Spacer(modifier = modifier.height(32.dp))
        }
    } else if(stage == SharingStage.Shared) {
        Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Check,null)
            Spacer(modifier = modifier.height(8.dp))
            Text(if(isSending) "Shared" else "Received", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = modifier.height(32.dp))
        }
    } else if (stage == SharingStage.Discovering){
        Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.preparing), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = modifier.height(8.dp))
            LinearProgressIndicator()
            Spacer(modifier = modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShareSheetPreview() {
    ShareSheet(
        huntWithCheckpoints = HuntWithCheckpoints(),
        dismissSheet = { },
        sharingViewModel = null
    )
}