package com.example.dweek04a.uicomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dweek04a.model.Item
import com.example.dweek04a.model.TodoItemFactory

@Composable
fun MainScreen(modifier: Modifier = Modifier) {

    val todoList = remember {
        // mutableStateListOf<Item>()
        // 빈 리스트로 선언할때는 위처럼
        TodoItemFactory.makeTodoList()
        // 기본 데이터를 가진 것으로 만들려면 factory에서 호출해서 받아오기
    }

    Column {
        TodoListTitle()
        TodoList(todoList)
        TodoItemInput(todoList)
    }
}
@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen()
    
}