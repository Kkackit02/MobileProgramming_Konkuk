package com.example.week14.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.week14.viewmodel.ItemEntity

@Composable
fun ItemList(list: List<ItemEntity>) {
    LazyColumn {
        items(list) { item ->
            ItemUI(item)
            HorizontalDivider(color = Color.Black, thickness = 2.dp)
        }
    }
}

@Composable
fun ItemUI(itemEntity: ItemEntity) {
    Column(modifier = Modifier
        .padding(10.dp)
        .fillMaxWidth()
    ) {
        Text(itemEntity.itemID.toString(), fontSize = 15.sp)
        Text(itemEntity.itemName, fontSize = 15.sp)
        Text(itemEntity.itemQuantity.toString(), fontSize = 15.sp)
    }
}