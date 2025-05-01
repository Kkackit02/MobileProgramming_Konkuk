package com.example.week09.navGraph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.week09.model.Routes
import com.example.week09.uicomponents.LoginScreen
import com.example.week09.uicomponents.Register
import com.example.week09.uicomponents.WelcomeScreen

@Composable
fun LoginNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Routes.Login.route
    ) {

        composable(route = Routes.Login.route) {
            LoginScreen(
                onWelcomeNavigate = { navController.navigate(Routes.Welcome.route) },
                onRegisterNavigate = { navController.navigate(Routes.Register.route) }
            )
        }

        composable(route = Routes.Welcome.route) {
            WelcomeScreen()
        }

        composable(route = Routes.Register.route) {
            Register()
        }
    }
}