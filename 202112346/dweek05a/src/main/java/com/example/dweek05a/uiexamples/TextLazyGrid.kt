package com.example.dweek05a.uiexamples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun TextLazyGrid(dataList: MutableList<String>, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        //columns = GridCells.Adaptive(minSize = 100.dp),
        //최소 사이즈 줄수있는데 지금 2개로 고정되어있음
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 맨뒤에 보면 한줄이 지금 Number로 되어있는데
        // items가 아니라 item으로 선언해서 maxlineSpan을 줌으로써
        // 맨 위 Grid를 통짜 item으로 설정가능
        item(span = { GridItemSpan(maxLineSpan) }){
            Text("Number")
        }
        //지금 지그재그로 설정되고있음
        items(dataList) { item ->
            TextCell(text = item, Modifier.background(Color.Green))
        }
    }
}


@Preview
@Composable
private fun TextLazyGridPreview() {
    val dataList = (1..30).map { it.toString() }.toMutableList()
    TextLazyGrid(dataList = dataList, modifier = Modifier.fillMaxSize())
}