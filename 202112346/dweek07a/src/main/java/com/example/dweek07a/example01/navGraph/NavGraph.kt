package com.example.dweek07a.example01.navGraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dweek07a.example01.model.Routes
import com.example.dweek07a.example01.uicomponents.HomeScreen
import com.example.dweek07a.example01.uicomponents.Screen_A
import com.example.dweek07a.example01.uicomponents.Screen_B
import com.example.dweek07a.example01.uicomponents.Screen_C
import com.example.dweek07a.example01.uicomponents.Screen_D

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = Routes.Home.route) {

        composable(route = Routes.Home.route) {
            // composabl을 통해 graph 생성
            // 람다식으로 생성해주고있음
            HomeScreen(
                //이제 composable 함수의 호출은 다 navigation에서 이뤄짐을 알 수 있음
                onNavigateA = { navController.navigate(Routes.ScreenA.route) },
                onNavigateB = { navController.navigate(Routes.ScreenB.route) }
            )
        }

        composable(route = Routes.ScreenA.route) {
            Screen_A(
                onNavigateC = { navController.navigate(Routes.ScreenC.route) },
                onNavigateD = { navController.navigate(Routes.ScreenD.route) })
        }

        composable(route = Routes.ScreenB.route) {
            Screen_B()
            //B는 어딘가로 이동하지않으니 아무것도 받지않음
        }

        composable(route = Routes.ScreenC.route) {
            Screen_C(onNavigateHome = {
                navController.navigate(Routes.Home.route) {
                    popUpTo(Routes.Home.route)
                    {
                        inclusive = true
                    }
                    launchSingleTop = true
                    //back stack을 초기화 해서 뒤로가기를 누르면 이전화면이 아니라
                    // 바로 홈으로 이동

                }
            })

        }


        composable(route = Routes.ScreenD.route) {
            Screen_D()

        }
    }
}