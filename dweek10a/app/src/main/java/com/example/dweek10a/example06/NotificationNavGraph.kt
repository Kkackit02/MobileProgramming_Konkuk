package com.example.dweek10a.example06

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

@Composable
fun NotificationNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "Main") {
        composable("Main") {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NotificationMainScreen()
            }
        }
        composable(
            route = "Msg?msg={msg}",
            arguments = listOf(
                navArgument("msg"){
                    type = NavType.StringType
                    defaultValue = "noMsg"
                }
            ),
            //외부접근을 위해 deepLinks 추가 , 내부 접근은 위 아래 코드로도 충분함
            deepLinks = listOf(
                navDeepLink {
                    //쿼리 파라미터
                    uriPattern = "myapp://greenjoahome.com?msg={msg}"
                    action = Intent.ACTION_VIEW
                },
                navDeepLink {
                    //경로 파라미터
                    uriPattern = "myapp://greenjoahome.com/{msg}"
                    action = Intent.ACTION_VIEW
                }
            )

        ){
            val msg = it.arguments?.getString("msg") ?: "noMsg"
            MsgShow(msg = msg)
        }
    }
}