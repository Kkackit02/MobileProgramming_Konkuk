package com.example.a2024midexam.model

import androidx.compose.runtime.saveable.listSaver

data class ItemData(

    val name: String,
    val number: String,
    var click: Int = 0)
