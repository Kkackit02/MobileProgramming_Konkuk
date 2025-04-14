package com.example.dweek05a.uiexamples

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun RandomColorButton() {
    val scope = rememberCoroutineScope()
    var color by remember { mutableStateOf(Color.Red) }

    Column {
        Button(
            onClick = {

                scope.launch { //코루틴 생성
                    while (true) {
                        //delay 앞을 보면 suspend가 함수 앞에 붙어있음.
                        //그래서 main thread안에서는 사용 불가능
                        delay(500)
                        //무한 루프면 너무 빨라서 Delay 주기
                        // 코루틴이기때문에 생성할 수 있음
                        color = Color(
                            //Color 값을 랜덤 생성 R,G,B,A
                            Random.nextInt(0xFF),
                            Random.nextInt(0xFF),
                            Random.nextInt(0xFF),
                            0xFF
                        )
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(color)
            //위에서 랜덤하게 생성한 값으로 변경
        ) {
            Text("Change Color")
        }
    }
}

@Composable
fun RandomColorButton2() {
    val scope = rememberCoroutineScope()
    var color by remember { mutableStateOf(Color.Red) }

    // 이쪽에는 생성할수없음
    // recomposition할때마다 계속해서 코루틴이 생성됨.
    // onClick같은 composable 함수가 아닌, 특정 이벤트에서 사용될 수 있게
    // recomposition에서 쓸수있는 couroutine은 따로 있으니
    // 그냥 Coroutine쓸때는 조심할 것!!
    Column {
        Button(
            onClick = {
                //1차 코루틴
               val job = scope.launch {
                    while (true) {
                        delay(500)
                        color = Color(
                            Random.nextInt(0xFF),
                            Random.nextInt(0xFF),
                            Random.nextInt(0xFF),
                            0xFF
                        )
                    }
                }

                //2차 코루틴(위에서 생성한 코루틴 취소)
                scope.launch {
                    delay(2000)
                    //위에서 먼저 만들었던 job(코루틴)을 중지
                    job.cancel()

                }
            },
            colors = ButtonDefaults.buttonColors(color)
        ) {
            Text("Change Color")
        }
    }
}

@Preview
@Composable
private fun RandomColorButtonPreview() {
    Column {
        RandomColorButton()
        RandomColorButton2()
    }

}