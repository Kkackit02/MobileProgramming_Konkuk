package com.example.dweek05a.uicomponents

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dweek05a.R
import com.example.dweek05a.model.ButtonType
import com.example.dweek05a.model.ImageData
import com.example.dweek05a.model.ImageUri
import com.example.dweek05a.viewmodel.ImageViewModel


@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    imageViewModel: ImageViewModel = viewModel()
) {
    val imageList = imageViewModel.imageList
    val orientation = LocalConfiguration.current.orientation

    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageList(imageList = imageList)
        }
    } else //가로 모드
    {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImageList(imageList = imageList)
        }
    }


    /*
    var img1State by rememberSaveable(stateSaver = ImageData.ImageSaver) {
        mutableStateOf(
            ImageData(
                image = ImageUri.ResImage(R.drawable.img1),
                buttonType = ButtonType.BADGE,
                likes = 50,
                dislikes = 10
            )
        )
    }

    var img2State by rememberSaveable(stateSaver = ImageData.ImageSaver) {
        mutableStateOf(
            ImageData(
                image = ImageUri.ResImage(R.drawable.img2),
                buttonType = ButtonType.EMOJI,
                likes = 100,
                dislikes = 10
            )
        )
    }




    Column {
        ImageWithButton(image = img1State.image) {
            ButtonWithBadge(likes = img1State.likes) {
                img1State = img1State.copy(likes = img1State.likes+1)
            }
        }

        ImageWithButton(image = img2State.image) {
            ButtonWithEmoji(
                likes = img2State.likes,
                dislikes = img2State.dislikes,
                onClickLikes = {
                    img2State = img2State.copy(likes = img2State.likes + 1)
                },
                onClickDisLikes = {
                    img2State = img2State.copy(dislikes = img2State.dislikes + 1)
                }
            )
        }
    }

     */

}