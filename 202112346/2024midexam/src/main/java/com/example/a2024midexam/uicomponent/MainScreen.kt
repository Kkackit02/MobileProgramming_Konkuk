package com.example.a2024midexam.uicomponent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a2024midexam.model.ItemData

@Composable
fun MainScreen(modifier: Modifier = Modifier) {

    //202112346 정근녕
    val itemList = remember {
        mutableStateListOf<ItemData>().apply {
            //addAll(TodoItemFactory.makeTodoList())
        }
    }

    val nameValue = remember {
        mutableStateOf("")
    }


    Column(modifier = Modifier.fillMaxSize()) {

        UserInputScreen(modifier=)

        Column(modifier = Modifier) // 목록 란
        {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "친구 목록")
            }

        }
    }
}


@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen()

}
