package com.example.week09.example03.uicomponents

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.week09.example03.model.Routes
import com.example.week09.example03.navigation.BottomNavigationBar
import com.example.week09.example03.navigation.NavGraph

@Composable
fun rememberViewModelStoreOwner(): ViewModelStoreOwner {
    val context = LocalContext.current
    return remember(context) { context as ViewModelStoreOwner }
}

val LocalNavGraphViewModelStoreOwner =
    staticCompositionLocalOf<ViewModelStoreOwner> {
        error("Undefined")
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen3(modifier: Modifier = Modifier) {

    val navStoreOwner = rememberViewModelStoreOwner()
    val navController = rememberNavController()

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination by remember(navBackStackEntry) {
        derivedStateOf {
            navBackStackEntry.value?.destination?.route?.let {
                Routes.getRoutes(it)
            } ?: run {
                Routes.Main
            }
        }
    }

    CompositionLocalProvider(
        LocalNavGraphViewModelStoreOwner provides navStoreOwner
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "20000 홍길동") }
                )
            },
            bottomBar = {
                if (currentDestination.isRoot)
                    BottomNavigationBar(navController)
            },
        ) { contentPadding ->
            NavGraph(navController = navController, modifier = Modifier.padding(contentPadding))
        }
    }
}