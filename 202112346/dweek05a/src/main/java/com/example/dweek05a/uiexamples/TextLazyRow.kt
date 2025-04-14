package com.example.dweek05a.uiexamples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TextLazyRow(dataList: MutableList<String>, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        //list에 data가 많이 들어있으면 lazy컴포저블을 쓰자~!
        items(dataList) { item ->
            TextCell(text = item, Modifier.background(Color.Green))
        }
    }
}

@Preview
@Composable
private fun TextLazyRowPreview() {
    val dataList = (1..30).map { it.toString() }.toMutableList()
    TextLazyRow(dataList = dataList, modifier = Modifier.fillMaxWidth())
}
