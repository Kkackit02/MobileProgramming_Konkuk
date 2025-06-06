package com.example.a2024midexam.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.a2024midexam.model.ItemData
import com.example.a2024midexam.model.UserItemFactory

class ItemViewModel : ViewModel() {
    //private val _itemList = UserItemFactory.makeUserItemList()
    private val _itemList = mutableStateListOf<ItemData>()
    var sortedList = mutableStateListOf<ItemData>()

    val itemList:MutableList<ItemData>
        get() = _itemList

    fun incrementClick(index: Int) {
        val old = _itemList[index]
        _itemList[index] = old.copy(click = old.click + 1)
    }

    fun addItem(name: String, number: String) {
        _itemList.add(ItemData(name, number, click = 0))
    }

    fun sortList() {
        val sortedList = _itemList.sortedBy { it.click }

        sortedList.forEachIndexed { index, item ->
            _itemList[index] = item.copy() // 상태 변경 인식
        }
    }
}