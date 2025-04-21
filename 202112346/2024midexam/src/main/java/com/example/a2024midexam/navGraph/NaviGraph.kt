package com.example.a2024midexam.navGraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.a2024midexam.model.ItemData
import com.example.a2024midexam.model.Routes
import com.example.a2024midexam.uicomponent.HomeScreen
import com.example.a2024midexam.uicomponent.MainScreen
import com.example.a2024midexam.uicomponent.SubScreen


@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = com.example.a2024midexam.model.Routes.Home.route
    ) {

        composable(route = com.example.a2024midexam.model.Routes.Home.route) {
            HomeScreen(
                onNavigateSubScreen = { userName, userNumber, userClick ->
                    navController.navigate(Routes.SubScreen.route + "?UserName=$userName&UserNum=$userNumber&UserClick=$userClick")


                }
            )
        }

        composable(
            route = Routes.SubScreen.route+ "?UserName={UserName}&UserNum={UserNum}&UserClick={UserClick}",
            arguments = listOf(
                navArgument(name = "UserName") {
                    type = NavType.StringType
                    defaultValue = "User"
                },
                navArgument(name = "UserNum") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(name = "UserClick") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("UserName") ?: ""
            val number = backStackEntry.arguments?.getString("UserNum") ?: ""
            val click = backStackEntry.arguments?.getInt("UserClick") ?: 0
            SubScreen(
                UserName = name,
                UserNumber = number,
                UserClick = click
            )

        }


    }
}