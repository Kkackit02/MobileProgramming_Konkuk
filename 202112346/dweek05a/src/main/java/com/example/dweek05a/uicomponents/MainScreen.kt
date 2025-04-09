package com.example.dweek05a.uicomponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.dweek05a.R
import com.example.dweek05a.model.ButtonType
import com.example.dweek05a.model.ImageData
import com.example.dweek05a.model.ImageUri


@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var img1State by rememberSaveable {
        mutableStateOf(
            ImageData(
                image = ImageUri.ResImage(R.drawable.img1),
                buttonType = ButtonType.BADGE,
                likes = 50,
                dislikes = 10
            )
        )
    }

    var img2State by rememberSaveable {
        mutableStateOf(
            ImageData(
                image = ImageUri.ResImage(R.drawable.img2),
                buttonType = ButtonType.EMOJI,
                likes = 20,
                dislikes = 0
            )
        )
    }

}