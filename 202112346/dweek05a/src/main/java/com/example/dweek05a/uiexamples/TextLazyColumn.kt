package com.example.dweek05a.uiexamples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TextLazyColumn(dataList: MutableList<String>, modifier: Modifier = Modifier) {
    // list를 받아서 column으로 생성
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(10.dp),
        //패딩을 줘서 화면 끝에서 잘리지않고 자연스럽게 화면 밖으로 사라지게 만듬
        verticalArrangement = Arrangement.spacedBy(10.dp),
        // 콘텐츠 사이는 10 dp씩 떨어짐
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //item들을 list 바탕으로 만드는데, text는 item의 index로
        items(items = dataList) {item->
            TextCell(text = item, Modifier.background(Color.Cyan))
        }
    }
}

@Preview
@Composable
private fun TextLazyColumnBasicPreview() {
    val dataList = (1..20).map { it.toString() }.toMutableList()
    TextLazyColumn(dataList = dataList, modifier = Modifier.fillMaxSize())
}

