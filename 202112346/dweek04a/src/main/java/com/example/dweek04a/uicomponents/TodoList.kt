package com.example.dweek04a.uicomponents

import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dweek04a.model.Item
import com.example.dweek04a.model.TodoItemFactory
import com.example.dweek04a.model.TodoStatus


@Composable
fun TodoList(todoList: MutableList<Item>, modifier: Modifier = Modifier) {

    val scrollState = rememberScrollState() //scroll의 상태를 유지하기 위해 변수 선언

    //WC로 자동 생성
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        todoList.forEachIndexed { index, item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row {
                    TodoCheckbox(checked = item.status == TodoStatus.COMPLETED) { checked ->
                        todoList[index] =
                            item.copy(status = if (checked) TodoStatus.COMPLETED else TodoStatus.PENDING)

                        //mutableStatusList에서 단순히 element의 값이 바뀌는걸로는 인식을 안함
                        //그래서 그냥 리스트 element 자체를 바꿔줘야함
                        //이를 위해 객체를 copy(매개변수가 너무 많아서 copy로 하고 특정 값만바꾸기)

                    }

                    TodoItem(item = item)
                }
            }
        }

    }

}

@Preview
@Composable
private fun TodoListPreview() {
    TodoList(TodoItemFactory.makeTodoList())
}