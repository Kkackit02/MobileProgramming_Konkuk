package com.example.a2024midexam.model

sealed class Routes(val route: String) {
    object Home : Routes("HomeScreen")
    object SubScreen : Routes("SubScreen")

}