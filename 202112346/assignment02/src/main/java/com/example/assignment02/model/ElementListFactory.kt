package com.example.assignment02.model

import androidx.compose.runtime.mutableStateListOf
import com.example.assignment02.R

object ElementListFactory {
    fun makeImageList() = mutableStateListOf(
        ElementData(
            image = R.drawable.arms,
            elementName = "arms"
        ),
        ElementData(
            image = R.drawable.ears,
            elementName = "ears",
            isActive = true
        ),
        ElementData(
            image = R.drawable.eyebrows,
            elementName = "eyebrows"
        ),ElementData(
            image = R.drawable.eyes,
            elementName = "eyes"
        ),
        ElementData(
            image = R.drawable.glasses,
            elementName = "glasses"
        ),
        ElementData(
            image = R.drawable.hat,
            elementName = "hat"
        ),
        ElementData(
            image = R.drawable.mouth,
            elementName = "mouth"
        ),
        ElementData(
            image = R.drawable.mustache,
            elementName = "mustache"
        ),
        ElementData(
            image = R.drawable.nose,
            elementName = "nose"
        ),
        ElementData(
            image = R.drawable.shoes,
            elementName = "shoes"
        )


    )

}
