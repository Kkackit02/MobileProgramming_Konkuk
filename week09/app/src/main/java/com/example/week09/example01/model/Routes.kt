package com.example.week09.example01.model


sealed class Routes (val route: String) {
    object Login : Routes("Login")
    object Register : Routes("Register")
    object Welcome : Routes("Welcome")
}