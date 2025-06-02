package com.example.week13

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.week13.ui.theme.Week13Theme

class MainActivity : ComponentActivity() {

//    val br = BatteryBR()
//    override fun onStart() {
//        super.onStart()
//        val intentFilter = IntentFilter(Intent.ACTION_POWER_CONNECTED)
//        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
//        this.registerReceiver(br, intentFilter)
//    }
//
//    override fun onStop() {
//        this.unregisterReceiver(br)
//        super.onStop()
//    }






    fun handleIntent(intent: Intent?)
    {
        //activity가 활성화되면 intent정보가 들어옴!!
        //물론 기본은 아무 값도 안들어오지만, 우리가 만들어서 넣어줄 수 있음
        val msgSender = intent?.getStringExtra("msgSender")
        val msgBody = intent?.getStringExtra("msgBody")
        //만약 intent에 정보가 없으면 그냥 null이 들어감
        setContent {
            Week13Theme {
                MainScreen(msgSender, msgBody)
            }
        }
    }


    //activity가 재사용 될 때 호출 되는 함수
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)

    }
    //activity가 새로 생성될때만 호출됨
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge() // status 바를 계속 표시하는 기능
        handleIntent(intent)
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
    Week13Theme {
        Greeting("Android")
    }
}