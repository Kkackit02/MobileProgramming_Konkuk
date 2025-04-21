package com.example.a2024midexam.uicomponent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a2024midexam.model.ItemData

@Composable
fun UserItem(item: ItemData, modifier: Modifier = Modifier) {
    Row {
        Text(
            text = item.name,
            fontSize = 16.sp,
        )

    }
}

@Preview
@Composable
private fun UserItemPrev() {
    Column {
        UserItem(ItemData("greenjoa1", "010-9066-5493"))
        UserItem(ItemData("greenjoa2", "010-9066-549232"))
    }
}