package com.example.a2024midexam.uicomponent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a2024midexam.model.ItemData
import com.example.a2024midexam.viewmodel.ItemViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun ItemInput(itemList: MutableList<ItemData>,itemViewModel: ItemViewModel = viewModel(), modifier: Modifier = Modifier) {
    var nameState by remember { mutableStateOf("") }
    var numberState by remember { mutableStateOf("") }

    val onNameChange = { text: String ->
        nameState = text
    }

    val onNumberTextChange = { text: String ->
        numberState = text
    }



    Column (
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = nameState,
            onValueChange = onNameChange,
            placeholder = { Text("친구이름") },
            modifier = Modifier
                .padding(end = 8.dp)
                .align(Alignment.CenterHorizontally)

        )
        TextField(
            value = numberState,
            onValueChange = onNumberTextChange,
            placeholder = { Text("전화번호") },
            modifier = Modifier
                .padding(end = 8.dp)
                .align(Alignment.CenterHorizontally)
        )

        Button(onClick = {
            itemViewModel.addItem(nameState, numberState)

            nameState = ""
            numberState = ""
        }, modifier = Modifier.align(Alignment.CenterHorizontally)



        ) {
            Text(text = "추가하기")
        }
    }
}