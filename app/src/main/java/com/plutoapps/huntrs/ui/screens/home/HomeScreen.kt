package com.plutoapps.huntrs.ui.screens.home

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import com.plutoapps.huntrs.R
import com.plutoapps.huntrs.data.models.Hunt
import com.plutoapps.huntrs.data.models.HuntWithCheckpoints
import com.plutoapps.huntrs.data.repos.PermissionsRepo
import com.plutoapps.huntrs.ui.composables.HuntSheet
import com.plutoapps.huntrs.ui.composables.ShareSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    hunts: List<Hunt>,
    upsertHunt: (HuntWithCheckpoints) -> Unit,
    getHunt: suspend (String) -> HuntWithCheckpoints,
    deleteHunt: (Hunt) -> Unit,
    loadHuntsByType: (Boolean) -> Unit,
    sharingViewModel: BluetoothSharingViewModel?
) {

    val context = LocalContext.current

    val permissionsList = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) else listOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        )



    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Bluetooth enabled
            // You can proceed with Bluetooth-related actions
        } else {
            // Bluetooth not enabled
            // Handle the case where the user did not enable Bluetooth

        }
    }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        Toast.makeText(
            context,
            it.values.toString(),
            Toast.LENGTH_LONG
        ).show()
        //val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        //val uri = Uri.fromParts("package", context.packageName, null)
        //intent.data = uri
        //context.startActivity(intent)

    }

    var id by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var huntToBeShared by rememberSaveable {
        mutableStateOf<HuntWithCheckpoints?>(null)
    }

    var showing by rememberSaveable {
        mutableStateOf("Mine")
    }

    var isSending by rememberSaveable {
        mutableStateOf(true)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val shareSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showShareBottomSheet by remember { mutableStateOf(false) }
    val dismissSheet: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showBottomSheet = false
            }
        }
    }
    val dismissShareSheet: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!shareSheetState.isVisible) {
                showShareBottomSheet = false
            }
        }
    }

    val shareHunt : () -> Unit = {
        if(ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED){

        }
    }

    LaunchedEffect(Unit){
        sharingViewModel?.setSaveHunt(upsertHunt)
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Face, null)
                Spacer(modifier = modifier.width(16.dp))
                Text(text = stringResource(id = R.string.app_name))
            }
        })
    },
        floatingActionButton = {

            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(onClick = {
                    isSending = false
                    if(PermissionsRepo(context,context as Activity).canAccessLocation()){
                        if(PermissionsRepo(context,context).canAccessBluetooth()){
                            showShareBottomSheet = true
                            scope.launch {
                                withContext(Dispatchers.Main){
                                    huntToBeShared = null
                                }
                                sheetState.expand()
                            }
                        }
                    }
                }) {
                    Icon(painterResource(id = R.drawable.baseline_connect_without_contact_24), null)
                }
                Spacer(modifier = modifier.height(16.dp))
                FloatingActionButton(onClick = {
                    showBottomSheet = true
                    id = null
                    scope.launch {
                        sheetState.expand()
                    }
                }) {
                    Icon(painterResource(id = R.drawable.baseline_post_add_24), null)
                }
            }
        }) { paddingValues ->
        if (showBottomSheet)
            ModalBottomSheet(onDismissRequest = dismissSheet, sheetState = sheetState) {
                HuntSheet(
                    id = id,
                    dismissSheet = dismissSheet,
                    upsertHunt = upsertHunt,
                    getHunt = getHunt
                )
            }
        if(showShareBottomSheet)
            ModalBottomSheet(onDismissRequest = dismissShareSheet,sheetState = shareSheetState) {
                ShareSheet(huntWithCheckpoints = huntToBeShared,dismissSheet = dismissSheet,sharingViewModel = sharingViewModel, isSending = isSending,)
            }
        Column(modifier = modifier.padding(paddingValues)) {
            Row(horizontalArrangement = Arrangement.Center, modifier = modifier.fillMaxWidth()) {
                for (filter in listOf("Mine", "Others"))
                    InputChip(
                        selected = filter == showing,
                        onClick = {
                            showing = filter
                            loadHuntsByType(filter == "Mine")
                        },
                        label = { Text(filter) },
                        leadingIcon = {
                            if (filter == showing) Icon(
                                Icons.Default.Check,
                                null
                            ) else null
                        },
                        modifier = modifier.padding(horizontal = 8.dp)
                    )
            }
            LazyColumn(
                modifier = modifier
                    .weight(1f)
            ) {
                items(hunts.size, key = { hunts[it].id }) {
                    val hunt = hunts[it]
                    val dismissState = rememberDismissState(
                        initialValue = DismissValue.Default,
                        positionalThreshold = { a -> a / 3 }
                    )
                    SwipeToDismiss(state = dismissState, background = {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ), modifier = modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { scope.launch { dismissState.reset() } }) {
                                    Icon(Icons.Default.Close, null)
                                }
                                Spacer(modifier = modifier.width(16.dp))
                                if (dismissState.targetValue == DismissValue.DismissedToStart)
                                    IconButton(onClick = {
                                        deleteHunt(hunt)
                                    }) {
                                        Icon(Icons.Default.Delete, null)
                                    }
                            }
                        }
                    }, dismissContent = {
                        Card(
                            modifier = modifier
                                .padding(16.dp)
                                .border(
                                    width = 1.dp,
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                .fillMaxWidth()
                                .clickable {
                                    id = hunt.id
                                    showBottomSheet = true
                                    scope.launch {
                                        sheetState.expand()
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                        ) {
                                Column(modifier = modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()) {
                                    Text(
                                        text = hunt.title,
                                        style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary)
                                    )
                                    if (hunt.description.isNotEmpty())
                                        Text(text = hunt.description,
                                            style = MaterialTheme.typography.titleMedium)
                                    Text(text = stringResource(id = R.string.created_on,SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault()).format(Date(hunt.time))),
                                        style = MaterialTheme.typography.titleSmall)
                                    Row {
                                        Button(onClick = { /*TODO*/ }) {
                                            Icon(Icons.Default.PlayArrow,null)
                                            Spacer(modifier = modifier.width(16.dp))
                                            Text( stringResource(if(hunt.isMine) R.string.test else R.string.play))
                                        }
                                        Spacer(modifier = modifier.width(16.dp))
                                        OutlinedButton(onClick = {
                                            if(PermissionsRepo(context,context as Activity).canAccessLocation()){
                                                if(PermissionsRepo(context,context).canAccessBluetooth()){
                                                    isSending = true
                                                    showShareBottomSheet = true
                                                    scope.launch {
                                                        withContext(Dispatchers.Main){
                                                            huntToBeShared = getHunt(hunt.id)
                                                            Log.d("beesh sharing",huntToBeShared.toString())
                                                        }
                                                        sheetState.expand()
                                                    }
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Default.Share,null)
                                            Spacer(modifier = modifier.width(16.dp))
                                            Text(stringResource(R.string.share))
                                        }
                                    }
                                }

                        }
                    }, directions = setOf(
                        DismissDirection.EndToStart
                    ))
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        hunts = emptyList(),
        upsertHunt = {},
        getHunt = { HuntWithCheckpoints() },
        deleteHunt = { },
        loadHuntsByType = { },
        sharingViewModel = null
    )
}