package com.example.assignment02.model

import android.media.Image
import androidx.compose.runtime.saveable.listSaver

data class ElementData(
    val image: Int,
    val elementName: String,
    var isActive :Boolean = false
) {}