package com.example.dweek05a.model

import androidx.compose.runtime.mutableStateListOf
import com.example.dweek05a.R

object ImageListFactory {
    fun makeImageList() = mutableStateListOf(
        ImageData(
            image = ImageUri.ResImage(R.drawable.img1),
            buttonType = ButtonType.BADGE,
            likes = 50
        ),
        ImageData(
            image = ImageUri.ResImage(R.drawable.img2),
            buttonType = ButtonType.ICON,
            likes = 100
        ),
        ImageData(
            image = ImageUri.ResImage(R.drawable.img3),
            buttonType = ButtonType.BADGE,
            likes = 20
        ),
        ImageData(
            image = ImageUri.ResImage(R.drawable.img2),
            buttonType = ButtonType.EMOJI,
            likes = 100,
            dislikes = 10
        ),
        ImageData(
            image = ImageUri.ResImage(R.drawable.img1),
            buttonType = ButtonType.ICON,
            likes = 120,
            dislikes = 13
        ),
        ImageData(
            image = ImageUri.ResImage(R.drawable.img3),
            buttonType = ButtonType.BADGE,
            likes = 12,
            dislikes = 53
        )


    )

}