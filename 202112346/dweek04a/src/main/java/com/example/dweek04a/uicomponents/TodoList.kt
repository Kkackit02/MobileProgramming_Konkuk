package com.example.dweek04a.uicomponents

import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dweek04a.model.Item
import com.example.dweek04a.model.TodoItemFactory
import com.example.dweek04a.model.TodoStatus

@Composable
fun TodoList(
    todoList: MutableList<Item>,
    isFilterActive: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val displayList = if (isFilterActive) {
        todoList.filter { it.status == TodoStatus.PENDING }
    } else {
        todoList
    }

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        displayList.forEach { item ->
            val index = todoList.indexOf(item) // 실제 원본 인덱스를 찾아야 함

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .toggleable(
                            value = (item.status == TodoStatus.COMPLETED),
                            onValueChange = { isChecked ->
                                todoList[index] = item.copy(
                                    status = if (isChecked) TodoStatus.COMPLETED else TodoStatus.PENDING
                                )
                            },
                            role = Role.Checkbox
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = (item.status == TodoStatus.COMPLETED),
                        onCheckedChange = { checked ->
                            todoList[index] = item.copy(
                                status = if (checked) TodoStatus.COMPLETED else TodoStatus.PENDING
                            )
                        }
                    )

                    TodoItem(item = item) // 텍스트 등
                }

            }
        }
    }
}


@Preview
@Composable
private fun TodoListPreview() {


    val isFilterActive = remember { mutableStateOf(false) }
    TodoList(
        todoList = TodoItemFactory.makeTodoList(),
        isFilterActive = isFilterActive.value
    )
}

