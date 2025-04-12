package com.example.assignment02.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.assignment02.model.ElementData
import com.example.assignment02.model.ElementListFactory

class CharacterViewModel : ViewModel() {

    private val _elementList = mutableStateListOf<ElementData>()

    val elementList: MutableList<ElementData>
        get() = _elementList
    init {
        _elementList.addAll(ElementListFactory.makeImageList())
    }
    fun toggleElement(index: Int, isActive: Boolean) {
        _elementList[index] = _elementList[index].copy(isActive = isActive)
    }

}