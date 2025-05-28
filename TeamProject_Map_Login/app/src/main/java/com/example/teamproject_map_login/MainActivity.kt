package com.example.teamproject_map_login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.teamproject_map_login.navGraph.LoginNavGraph
import com.example.teamproject_map_login.ui.theme.TeamProject_Map_LoginTheme
import com.example.teamproject_map_login.uicomponents.LoginScreen
import com.example.teamproject_map_login.uicomponents.StartScreen
import com.example.teamproject_map_login.uicomponents.WelcomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeamProject_Map_LoginTheme {
                StartScreen()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TeamProject_Map_LoginTheme {
        Greeting("Android")
    }
}