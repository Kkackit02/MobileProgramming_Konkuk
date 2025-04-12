package com.example.assignment02.uicomponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.assignment02.R
import com.example.assignment02.model.ElementData

@Composable
fun ElementList(
    modifier: Modifier,
    elementList: MutableList<ElementData>,
    onCheckedChange: (Int, Boolean) -> Unit,
    displayMode: Boolean // false면 세로, true 면 가로
) {
    Text("202112346 정근녕")
    when (displayMode) {
        false -> {

            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(top = 150.dp)
                        .fillMaxWidth()
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.body),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()

                    )
                    elementList.forEachIndexed { index, ElementData ->
                        if (ElementData.isActive) {
                            Image(
                                painter = painterResource(id = ElementData.image),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )


                        }
                    }
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally

                    ) {
                    elementList.chunked(2).forEach { rowElements ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            rowElements.forEach { elementData ->
                                val index = elementList.indexOf(elementData)

                                ElementCheckBox(
                                    checked = elementData.isActive,
                                    onCheckedChange = { isChecked ->
                                        onCheckedChange(index, isChecked)
                                    },
                                    elementName = elementData.elementName,
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            if (rowElements.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }


                }
            }
        }

        true -> {

            Row(modifier = Modifier) {
                Box(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxSize()
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.body),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    elementList.forEachIndexed { index, ElementData ->
                        if (ElementData.isActive) {
                            Image(
                                painter = painterResource(id = ElementData.image),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )


                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    elementList.chunked(2).forEach { rowElements ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowElements.forEach { elementData ->
                                val index = elementList.indexOf(elementData)

                                ElementCheckBox(
                                    checked = elementData.isActive,
                                    onCheckedChange = { isChecked ->
                                        onCheckedChange(index, isChecked)
                                    },
                                    elementName = elementData.elementName,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (rowElements.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }


                }
            }
        }


    }
}