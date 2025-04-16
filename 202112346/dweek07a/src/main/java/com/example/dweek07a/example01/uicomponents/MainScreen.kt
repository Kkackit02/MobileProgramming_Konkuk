package com.example.dweek07a.example01.uicomponents

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.dweek07a.example01.navGraph.NavGraph

@SuppressLint("RestrictedApi")
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    //destination이 바뀔때마다 이벤트 호출
    navController.addOnDestinationChangedListener()
    { _, _, _ ->
        //currentBackStack은 제한된 API, 함부로 막 변경할 수 없음
        //이때 suppressLint 를 하면 그냥 경고 강제 무시
        navController.currentBackStack.value.forEachIndexed { index, entry ->
            Log.d("backStack", "$index ${entry.destination.route}")

        }

    }

    NavGraph(navController = navController)
    //여기가 이제 메인인것
}