package com.example.dweek05a.uiexamples

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(80.dp, 80.dp)
            .border(width = 4.dp, color = Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun TextCellRandomSize(text: String, modifier: Modifier = Modifier) {
    val cellSize = (50..100).random()
    //box의 사이즈를 랜덤하게 지정
    Box(
        modifier = modifier
            .size(cellSize.dp)
            .border(width = 4.dp, color = Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Preview
@Composable
fun TextCellPreview() {
    Column {
        TextCell(text = "1", Modifier.background(Color.Green))
        TextCell(text = "2", Modifier.background(Color.Cyan))
        TextCellRandomSize(text = "3", Modifier.background(Color.Yellow))
    }
}



