package com.example.teamproject_map_login.navGraph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.teamproject_map_login.model.Routes
import com.example.teamproject_map_login.uicomponents.LoginScreen
import com.example.teamproject_map_login.uicomponents.RegisterScreen
import com.example.teamproject_map_login.uicomponents.WelcomeScreen
@Composable
fun LoginNavGraph(navController: NavHostController) {

    NavHost(navController = navController, startDestination = Routes.Login.route) {

        composable(route = Routes.Login.route) {
            LoginScreen(
                onWelcomeNavigate = { userId ->
                    navController.navigate(Routes.Welcome.route + "/$userId")
                },
                onRegisterNavigate = { _, _ ->
                    navController.navigate(Routes.Register.route)
                }
            )
        }

        composable(
            route = Routes.Welcome.route + "/{userID}",
            arguments = listOf(
                navArgument(name = "userID") {
                    type = NavType.StringType
                }
            )
        ) {
            WelcomeScreen(
                it.arguments?.getString("userID")
            )
        }

        composable(route = Routes.Register.route) {
            RegisterScreen(
                onLoginNavigate = {
                    navController.popBackStack() // 뒤로 가기
                }
            )
        }
    }
}
