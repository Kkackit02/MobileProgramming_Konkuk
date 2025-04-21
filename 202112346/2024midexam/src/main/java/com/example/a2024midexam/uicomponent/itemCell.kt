package com.example.a2024midexam.uicomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.a2024midexam.model.ItemData

@Composable
fun itemCell(
    itemData: ItemData,
    onUserClick: () -> Unit,
    onHeartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Yellow)
            .clickable { onUserClick() },
        verticalAlignment = Alignment.CenterVertically
    )
    {


        Text(text = itemData.name, modifier = Modifier.weight(1f))
        //남는 공간 전부 차지 → 하트는 우측 정렬됨
        IconButton(onClick = { onHeartClick() }) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "favorite",
                tint = when {
                    itemData.click >= 5 -> Color.Red
                    itemData.click >= 3 -> Color.Green
                    else -> Color.LightGray
                }
            )
        }

    }


}