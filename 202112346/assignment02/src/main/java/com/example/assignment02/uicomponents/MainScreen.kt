package com.example.assignment02.uicomponents

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.assignment02.viewmodel.CharacterViewModel


@Composable
fun MainScreen(
    modifier: Modifier = Modifier, characterViewModel: CharacterViewModel = viewModel()
) {
    val elementList = characterViewModel.elementList
    val orientation = LocalConfiguration.current.orientation


    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ElementList(
                modifier = Modifier,
                onCheckedChange = { index, isChecked ->
                    characterViewModel.toggleElement(index, isChecked)
                },
                elementList = elementList,
                displayMode = false
            )
            //ImageList(imageList = imageList)
        }
    } else //가로 모드
    {
        Row(
            modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically
        ) {
            ElementList(
                modifier = Modifier,
                onCheckedChange = { index, isChecked ->
                    characterViewModel.toggleElement(index, isChecked)
                },
                elementList = elementList,
                displayMode = true
            )

        }
    }
}


