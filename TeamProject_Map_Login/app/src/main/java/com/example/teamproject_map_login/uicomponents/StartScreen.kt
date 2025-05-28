package com.example.teamproject_map_login.uicomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.teamproject_map_login.navGraph.LoginNavGraph

@Composable
fun StartScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    LoginNavGraph(navController= navController)
}