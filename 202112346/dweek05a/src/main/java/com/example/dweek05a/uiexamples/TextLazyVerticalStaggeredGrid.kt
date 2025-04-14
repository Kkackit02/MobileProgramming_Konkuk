package com.example.dweek05a.uiexamples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TextLazyVerticalStaggeredGrid(dataList: List<String>, modifier: Modifier = Modifier) {
    //Staggered는 각 셀의 SIZE를 다르게 지정 할 수 있음.

    LazyVerticalStaggeredGrid(
        //columns = StaggeredGridCells.Adaptive(100.dp),
        columns = StaggeredGridCells.Fixed(2),
        verticalItemSpacing = 10.dp,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(10.dp),
        content = {
            items(dataList) { item ->
                TextCellRandomSize(text = item, Modifier.background(Color.Green))
                // 그냥 TextCell이 아니라
                // TextCellRandomSize로 만들어서
                // 크기가 다르게 생성
                // 근데 지금 보면 1 2 3 4 가 아니라, 1 2 4 3인데
                // 크기에 따른 ordering 순서(자체 기준)으로 동작함
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Preview
@Composable
private fun TextLazyVerticalStaggeredGridPreview() {
    val dataList = (1..30).map { it.toString() }.toMutableList()
    TextLazyVerticalStaggeredGrid(dataList = dataList, modifier = Modifier.fillMaxSize())
}