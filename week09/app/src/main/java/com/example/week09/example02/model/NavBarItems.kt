package com.example.week09.example02.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person

object NavBarItems{
    val BarItems = listOf(
        BarItem(
            title = Routes.Home.route,
            selectIcon = Icons.Default.Home,
            onSelectedIcon = Icons.Outlined.Home,
            route = Routes.Home.route
        ),
        BarItem(
            title = Routes.Contacts.route,
            selectIcon = Icons.Default.Person,
            onSelectedIcon = Icons.Outlined.Person,
            route = Routes.Contacts.route
        ),
        BarItem(
            title = Routes.Favorites.route,
            selectIcon = Icons.Default.Favorite,
            onSelectedIcon = Icons.Outlined.FavoriteBorder,
            route = Routes.Favorites.route,
        )
    )
}