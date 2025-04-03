package com.example.dweek04a.uicomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dweek04a.model.Item
import com.example.dweek04a.model.TodoItemFactory
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.dweek04a.model.TodoStatus

@Composable
fun MainScreen(modifier: Modifier = Modifier) {

    //202112346 정근녕
    val todoList = remember {
        mutableStateListOf<Item>().apply {
            addAll(TodoItemFactory.makeTodoList())
        }
    }

    val isFilterActive = remember { mutableStateOf(false) }

    // 필터링된 리스트 생성
    val filteredList = if (isFilterActive.value) {
        todoList.filter { it.status == TodoStatus.PENDING }
    } else {
        todoList
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .weight(6f)
                .fillMaxWidth()
        ) {
            TodoListTitle()

            // Switch 표시
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
                ) {
                TodoSwitch(
                    checked = isFilterActive.value,
                    onCheckedChange = { isFilterActive.value = it }
                )

            }

            Spacer(modifier = Modifier.height(8.dp))

            TodoList(
                    todoList = todoList,
            isFilterActive = isFilterActive.value
            )

        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TodoItemInput(todoList)
        }
    }
}


@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen()

}