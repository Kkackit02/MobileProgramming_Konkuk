package com.example.week09.example03.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.week09.example03.model.Routes
import com.example.week09.example03.uicomponents.login.LoginScreen
import com.example.week09.example03.uicomponents.login.Register
import com.example.week09.example03.uicomponents.login.WelcomeScreen


@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {

    NavHost(navController = navController,
        startDestination = Routes.User.route) {
        navigation(
            startDestination = Routes.Login.route,
            route = Routes.User.route
        ) {
            composable(route = Routes.Login.route) {
                LoginScreen(
                    onWelcomeNavigate = { navController.navigate(Routes.Welcome.route) },
                    onRegisterNavigate = { navController.navigate(Routes.Register.route) }
                )
            }

            composable(route = Routes.Welcome.route) {
                WelcomeScreen(
                    onNavigateMain = {
                        navController.navigate(Routes.Main.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(route = Routes.Register.route) {
                Register()
            }
        }
        mainNavGraph(navController)
    }
}

