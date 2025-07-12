package com.example.teamproject_map_login.model

sealed class Routes(val route: String) {
    object Login : Routes("LoginScreen")
    object Welcome : Routes("WelcomeScreen")
    object Register : Routes("RegisterScreen")
    object Map : Routes("NaverMapScreen04")
}