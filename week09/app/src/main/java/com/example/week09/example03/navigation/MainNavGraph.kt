package com.example.week09.example03.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.week09.example03.model.Routes
import com.example.week09.example03.uicomponents.main.Contacts
import com.example.week09.example03.uicomponents.main.Favorites
import com.example.week09.example03.uicomponents.main.Home


fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.Home.route,
        route = Routes.Main.route
    ) {

        composable(route = Routes.Home.route) {
            Home()
        }

        composable(
            route = Routes.Favorites.route
        ) {
            Favorites()
        }

        composable(route = Routes.Contacts.route) { it ->
            Contacts()
        }
    }
}