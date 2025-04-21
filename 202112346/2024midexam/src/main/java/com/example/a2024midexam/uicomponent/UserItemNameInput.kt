package com.example.a2024midexam.uicomponent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a2024midexam.model.ItemData
import com.example.a2024midexam.model.UserItemFactory


@Composable
fun UserItemNameInput(value : MutableState<String> , modifier: Modifier = Modifier) {
    var textState by remember { mutableStateOf("") }
    val onTextChange = { text: String ->
        textState = text

    }



    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = textState,
            onValueChange = onTextChange,
            placeholder = { Text("친구 이름") },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )

    }
}

@Preview
@Composable
private fun UserItemInputPreview() {
    UserItemNameInput()
}