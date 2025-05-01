package com.example.week09.model


sealed class Routes (val route: String) {
    object Login : Routes("Login")
    object Register : Routes("Register")
    object Welcome : Routes("Welcome")
}