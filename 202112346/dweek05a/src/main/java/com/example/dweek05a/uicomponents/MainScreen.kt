package com.example.dweek05a.uicomponents

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dweek05a.R
import com.example.dweek05a.model.ButtonType
import com.example.dweek05a.model.ImageData
import com.example.dweek05a.model.ImageUri
import com.example.dweek05a.uiexamples.ScrollToTopButton
import com.example.dweek05a.uiexamples.TextCell
import com.example.dweek05a.viewmodel.ImageViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


@Composable
fun MainScreen(
    modifier: Modifier = Modifier, imageViewModel: ImageViewModel = viewModel()
) {
    val imageList = imageViewModel.imageList

    val orientation = LocalConfiguration.current.orientation
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val showButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0

        }
    }
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        LazyColumn(
            modifier = modifier, contentPadding = PaddingValues(10.dp),
            //패딩을 줘서 화면 끝에서 잘리지않고 자연스럽게 화면 밖으로 사라지게 만듬
            verticalArrangement = Arrangement.spacedBy(10.dp),
            // 콘텐츠 사이는 10 dp씩 떨어짐
            horizontalAlignment = Alignment.CenterHorizontally, state = listState
        ) {

            //item들을 list 바탕으로 만드는데, text는 item의 index로
            itemsIndexed(items = imageList) { index, item ->
                when (item.buttonType) {
                    ButtonType.ICON -> {
                        ImageWithButton(image = item.image) {
                            ButtonWithBadge(likes = item.likes) {
                                imageList[index] = item.copy(likes = item.likes + 1)
                            }
                        }
                    }

                    ButtonType.BADGE -> {
                        ImageWithButton(image = item.image) {
                            ButtonWithBadge(likes = item.likes) {
                                imageList[index] = item.copy(likes = item.likes + 1)
                            }
                        }
                    }

                    ButtonType.EMOJI -> {
                        ImageWithButton(image = item.image) {
                            ButtonWithEmoji(
                                likes = item.likes,
                                dislikes = item.dislikes,
                                onClickLikes = {
                                    imageList[index] = item.copy(likes = item.likes + 1)
                                },
                                onClickDisLikes = {
                                    imageList[index] = item.copy(dislikes = item.dislikes + 1)
                                })
                        }
                    }
                }
            }
        }
    } else {
        LazyRow(
            modifier = modifier, contentPadding = PaddingValues(10.dp),
            //패딩을 줘서 화면 끝에서 잘리지않고 자연스럽게 화면 밖으로 사라지게 만듬
            verticalAlignment = Alignment.CenterVertically,
            // 콘텐츠 사이는 10 dp씩 떨어짐
            horizontalArrangement = Arrangement.spacedBy(10.dp), state = listState
        ) {
            //item들을 list 바탕으로 만드는데, text는 item의 index로
            itemsIndexed(items = imageList) { index, item ->
                when (item.buttonType) {
                    ButtonType.ICON -> {
                        ImageWithButton(image = item.image) {
                            ButtonWithBadge(likes = item.likes) {
                                imageList[index] = item.copy(likes = item.likes + 1)
                            }
                        }
                    }

                    ButtonType.BADGE -> {
                        ImageWithButton(image = item.image) {
                            ButtonWithBadge(likes = item.likes) {
                                imageList[index] = item.copy(likes = item.likes + 1)
                            }
                        }
                    }

                    ButtonType.EMOJI -> {
                        ImageWithButton(image = item.image) {
                            ButtonWithEmoji(
                                likes = item.likes,
                                dislikes = item.dislikes,
                                onClickLikes = {
                                    imageList[index] = item.copy(likes = item.likes + 1)
                                },
                                onClickDisLikes = {
                                    imageList[index] = item.copy(dislikes = item.dislikes + 1)
                                })
                        }
                    }
                }
            }
        }
    }
    AnimatedVisibility(visible = showButton) {
        ScrollToTopButton({
            scope.launch {
                listState.animateScrollToItem(0)
            }
        })
    }

    /*

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
    */


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