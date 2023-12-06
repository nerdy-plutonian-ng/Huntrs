package com.plutoapps.huntrs.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plutoapps.huntrs.R
import com.plutoapps.huntrs.data.models.Hunt
import com.plutoapps.huntrs.data.models.HuntWithCheckpoints
import com.plutoapps.huntrs.ui.composables.HuntSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    hunts: List<Hunt>,
    upsertHunt: (HuntWithCheckpoints) -> Unit,
    getHunt: suspend (String) -> HuntWithCheckpoints,
    deleteHunt: (Hunt) -> Unit
) {

    var id by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var showing by rememberSaveable {
        mutableStateOf<String>("All")
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val dismissSheet: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showBottomSheet = false
            }
        }
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
            ExtendedFloatingActionButton(onClick = {
                showBottomSheet = true
                id = null
                scope.launch {
                    sheetState.expand()
                }
            }) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = modifier.width(8.dp))
                Text(text = stringResource(R.string.hunt))
            }
        }) { paddingValues ->
        if (showBottomSheet)
            ModalBottomSheet(onDismissRequest = dismissSheet, sheetState = sheetState) {
                HuntSheet(id = id, dismissSheet = dismissSheet,upsertHunt=upsertHunt,getHunt=getHunt)
            }
        Column(modifier = modifier.padding(paddingValues)) {
            Row(horizontalArrangement = Arrangement.Center, modifier = modifier.fillMaxWidth()) {
                for(filter in listOf("All","Mine","Others"))
                    InputChip(
                        selected = filter == showing,
                        onClick = { showing = filter} ,
                        label = { Text(filter) },
                        leadingIcon = { if(filter == showing) Icon(Icons.Default.Check,null) else null },
                modifier = modifier.padding(horizontal = 8.dp))
            }
            LazyColumn(
                modifier = modifier
                    .weight(1f)
            ) {
                items(hunts.size, key = { hunts[it].id }) {
                    val hunt = hunts[it]
                    val dismissState = rememberDismissState(
                        initialValue = DismissValue.Default,
                        positionalThreshold = {a -> a/3}
                    )
                    SwipeToDismiss(state = dismissState, background = {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),modifier = modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { scope.launch { dismissState.reset() } }) {
                                    Icon(Icons.Default.Close,null)
                                }
                                Spacer(modifier = modifier.width(16.dp))
                                if(dismissState.targetValue == DismissValue.DismissedToStart)
                                    IconButton(onClick = {
                                        deleteHunt(hunt)
                                    }) {
                                        Icon(Icons.Default.Delete,null)
                                    }
                            }
                        }
                    } , dismissContent = {Card(
                        modifier = modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .clickable {
                                id = hunt.id
                                showBottomSheet = true
                                scope.launch {
                                    sheetState.expand()
                                }
                            }
                    ) {
                        Column(modifier = modifier.padding(16.dp)) {
                            Text(
                                text = hunt.title,
                                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary)
                            )
                            if(hunt.description.isNotEmpty())
                                Text(text = hunt.description)
                            Row {
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(Icons.Default.Edit, null)
                                }
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(Icons.Default.Share, null)
                                }
                            }
                        }
                    }})
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
        getHunt = {HuntWithCheckpoints()},
        deleteHunt = {  }
    )
}