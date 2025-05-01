package com.example.week09.uicomponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.compose.rememberNavController
import com.example.week09.navGraph.LoginNavGraph

@Composable
fun rememberViewModelStoreOwner(): ViewModelStoreOwner {
    val context = LocalContext.current

    return remember(context) { context as ViewModelStoreOwner }


}

val LocalNavGraphViewModelStoreOwner = staticCompositionLocalOf<ViewModelStoreOwner> {
    error("Undefined")
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val viewModelStoreOwner = rememberViewModelStoreOwner()

    CompositionLocalProvider(LocalNavGraphViewModelStoreOwner provides viewModelStoreOwner) {
        LoginNavGraph(navController = navController)
    }
}