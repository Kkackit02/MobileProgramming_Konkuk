package com.example.week09.example03.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person

object NavBarItems{
    val BarItems = listOf(
        BarItem(
            title = "Home",
            selectIcon = Icons.Default.Home,
            onSelectedIcon = Icons.Outlined.Home,
            route = "Home"
        ),
        BarItem(
            title = "Contacts",
            selectIcon = Icons.Default.Person,
            onSelectedIcon = Icons.Outlined.Person,
            route = "Contacts"
        ),
        BarItem(
            title = "Favorites",
            selectIcon = Icons.Default.Favorite,
            onSelectedIcon = Icons.Outlined.Favorite,
            route = "Favorites"
        )
    )
}