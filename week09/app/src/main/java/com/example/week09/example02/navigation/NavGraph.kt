package com.example.week09.example02.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.week09.example02.model.Routes
import com.example.week09.example02.uicomponents.AddContacts
import com.example.week09.example02.uicomponents.Contacts
import com.example.week09.example02.uicomponents.Favorites
import com.example.week09.example02.uicomponents.Home

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ){
        composable(Routes.Home.route){
            Home()
        }
        composable(Routes.Contacts.route){
            Contacts()
        }
        composable(Routes.Favorites.route){
            Favorites()
        }
        composable(Routes.AddContacts.route){
            AddContacts()
        }
    }
}