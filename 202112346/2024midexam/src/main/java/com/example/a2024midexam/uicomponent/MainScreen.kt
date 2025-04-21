package com.example.a2024midexam.uicomponent

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.Navigator
import androidx.navigation.compose.rememberNavController
import com.example.a2024midexam.model.Routes
import com.example.a2024midexam.navGraph.NavGraph

@Composable
// 가장 기본 start 스크린
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavGraph(navController = navController)
    //여기가 이제 메인인것
}