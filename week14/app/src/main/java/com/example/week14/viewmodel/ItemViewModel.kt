package com.example.week14.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItemViewModelFactory(private val repository: ItemRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ItemViewModel::class.java)){
            return ItemViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class ItemViewModel(private val repository: ItemRepository): ViewModel() {
    private var _itemList = MutableStateFlow<List<ItemEntity>>(emptyList())
    val itemList = _itemList.asStateFlow()

    init{
        getAllItems()
    }

    fun getAllItems() {
        viewModelScope.launch {
            repository.getAllItems().collect {
                _itemList.value = it
            }
        }
    }

    fun getItems(itemName:String) {
        viewModelScope.launch {
            repository.getItems(itemName).collect {
                _itemList.value = it
            }
        }
    }
    fun InsertItem(itemEntity: ItemEntity) {
        viewModelScope.launch {
            repository.InsertItem(itemEntity)

        }
    }
    fun UpdateItem(itemEntity: ItemEntity) {
        viewModelScope.launch {
            repository.UpdateItem(itemEntity)

        }
    }
    fun DeleteItem(itemEntity: ItemEntity) {
        viewModelScope.launch {
            repository.DeleteItem(itemEntity)

        }
    }
    fun SortItems() {
        viewModelScope.launch {
            repository.getSortedItems().collect {
                _itemList.value = it
            }
        }


    }

}