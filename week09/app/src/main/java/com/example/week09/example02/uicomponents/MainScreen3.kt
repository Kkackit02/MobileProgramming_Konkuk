package com.example.week09.example02.uicomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.week09.example02.model.Routes
import com.example.week09.example02.navigation.BottomNavigationBar
import com.example.week09.example02.navigation.NavGraph
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen3(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute by remember(backStackEntry)
    {
        derivedStateOf {
            backStackEntry?.destination?.route?.let {
                Routes.getRoutes(it)
            } ?: run {
                        Routes.Home
            }
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent()
            }
        }
    ) {


    Scaffold(
        topBar = {
            if(currentRoute.isRoot)
                TopAppBar(
                    title = { Text(text = currentRoute.route)},

                        navigationIcon =  {
                            IconButton(onClick = {
                                coroutineScope.launch{
                                    if(drawerState.isOpen)
                                    {

                                        drawerState.close()
                                    }
                                    else
                                    {
                                        drawerState.open()
                                    }
                                }
                            }) {
                                Icon(imageVector = Icons.Default.Menu,
                                    contentDescription = "")
                            }
                        }
                )
            else
                TopAppBar(
                    title = {},
                    navigationIcon =  {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "")
                        }
                    }
                )
        },
        bottomBar = {
            if(currentRoute.isRoot)
                BottomNavigationBar(navController)
        },
        floatingActionButton = {
            if(currentRoute == Routes.Contacts)
            {
                FloatingActionButton(onClick = {
                    navController.navigate(Routes.AddContacts.route)
                })
                {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "")
                }
            }
        }

    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            NavGraph(navController = navController)
        }
    }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    MaterialTheme {
        MainScreen3()
    }
}