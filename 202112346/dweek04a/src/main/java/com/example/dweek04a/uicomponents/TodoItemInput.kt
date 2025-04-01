package com.example.dweek04a.uicomponents

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dweek04a.model.Item
import com.example.dweek04a.model.TodoItemFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TodoItemInput(todoList: MutableList<Item>, modifier: Modifier = Modifier) {
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
            placeholder = { Text("할 일을 입력하세요") },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        Button(onClick = {
            todoList.add(
                Item(
                    textState, LocalDateTime
                        .now()
                        .format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
                )
            )
            textState = ""
        },
            modifier = Modifier.align(Alignment.CenterVertically)

        ) {
            Text(text = "추가")
        }
    }
}

@Preview
@Composable
private fun TodoItemInputPreview() {
    TodoItemInput(TodoItemFactory.makeTodoList())
}