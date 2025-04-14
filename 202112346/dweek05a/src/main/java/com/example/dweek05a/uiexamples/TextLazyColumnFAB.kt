package com.example.dweek05a.uiexamples

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun TextLazyColumnFAB(dataList: MutableList<String>, modifier: Modifier = Modifier) {
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()


    val showButton by remember {
        // 근데 여기서 내가 mutableStateOf로 선언을 하면
        // 시작에서 false가 되면
        // 아무리 scroll이 되어도 바뀌지않음
        // 이걸 ==으로 하면 recomposition이 될때마다 계산이 되니까..
        // 다른 상태에 의해서 변경될때만 계산하기 위해서
        // derivedStateOf를 사용
        // 즉 이 내부값이 바뀔때만 호출되는 것
        // true상태인 동안에는 안바뀌고 false가 될때만 값을 변경함
        derivedStateOf {
            state.firstVisibleItemIndex > 0
            // 처음 보여지는 값의 index값이 0보다 크다
            // 즉 스크롤이 이뤄졌다면 true상태가 되는 것

        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = state,
            modifier = modifier,
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items = dataList) {item->
                TextCell(text = item, Modifier.background(Color.Green))
            }
        }
        //visible이 ShowButton의 boolean값에 따라
        //각 버튼들에 애니메이션 적용
        AnimatedVisibility(visible = showButton) {
            ScrollToTopButton {
                scope.launch {
                    //scrollToItem도 suspend
                    //그래서 그냥 일반적인 composable함수에서는 못씀
                    //그래서 코루틴 내부에서 선언
                    state.scrollToItem(0)
                    //scroll위치를 0로(맨위로)
                }
            }
        }
    }
}

@Preview
@Composable
private fun TextLazyColumnFABPreview() {
    val dataList = (1..15).map { it.toString() }.toMutableList()
    TextLazyColumnFAB(dataList = dataList, modifier = Modifier.fillMaxSize())
}