package com.example.week14.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.week14.viewmodel.ItemRepository
import com.example.week14.viewmodel.ItemViewModel
import com.example.week14.viewmodel.ItemViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.database.database

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val table = Firebase.database.getReference("Products/items")
    val itemViewModel: ItemViewModel = viewModel(factory = ItemViewModelFactory(ItemRepository(table)))
    val itemListState by itemViewModel.itemList.collectAsState(initial = emptyList())
    Column {
        InputScreen(viewModel = itemViewModel)
        ItemList(list = itemListState)
    }
}