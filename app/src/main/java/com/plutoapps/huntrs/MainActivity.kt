package com.plutoapps.huntrs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plutoapps.huntrs.data.models.Hunt
import com.plutoapps.huntrs.data.models.HuntWithCheckpoints
import com.plutoapps.huntrs.data.repos.HuntsRoomRepo
import com.plutoapps.huntrs.ui.routes.Routes
import com.plutoapps.huntrs.ui.screens.home.HomeScreen
import com.plutoapps.huntrs.ui.screens.home.HuntsViewModel
import com.plutoapps.huntrs.ui.screens.home.HuntsViewModelFactory
import com.plutoapps.huntrs.ui.theme.AppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                HuntrsApp()
            }
        }
    }
}

@Composable
fun HuntrsApp() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val db = HuntrsApplication.db
    val repo = HuntsRoomRepo(db!!.getHuntsDao(), db.getCheckpointDao())
    val factory = HuntsViewModelFactory(repository = repo)
    val viewModel = ViewModelProvider(
        LocalContext.current as ViewModelStoreOwner,
        factory
    )[HuntsViewModel::class.java]
    val state = viewModel.getHunts().collectAsState(initial = emptyList())

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
            HomeScreen(hunts = state.value, upsertHunt = upsertHunt,getHunt = getHunt,deleteHunt = deleteHunt)
        }
    }
}

@Preview
@Composable
fun HuntrsAppPreview() {
    HuntrsApp()
}