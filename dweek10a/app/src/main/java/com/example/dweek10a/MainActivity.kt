package com.example.dweek10a

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dweek10a.example03.MainScreen04
import com.example.dweek10a.example04.MainScreen05
import com.example.dweek10a.example05.NotificationApp
import com.example.dweek10a.example06.NotificationNavGraph
import com.example.dweek10a.ui.theme.MyApp1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp1Theme {
                MainScreen04()
                //NotificationNavGraph()
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
    MyApp1Theme {
        Greeting("Android")
    }
}