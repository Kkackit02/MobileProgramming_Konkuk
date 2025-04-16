package com.example.dweek07a.example01.model

sealed class Routes(val route: String) {
    object Home : Routes(route = "Home")
    object ScreenA : Routes(route = "A")
    object ScreenB : Routes(route = "B")
    object ScreenC : Routes(route = "C")
    object ScreenD : Routes(route = "D")
}