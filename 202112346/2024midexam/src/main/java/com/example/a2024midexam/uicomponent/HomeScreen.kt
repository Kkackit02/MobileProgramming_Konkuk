package com.example.a2024midexam.uicomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a2024midexam.navGraph.NavGraph
import com.example.a2024midexam.viewmodel.ItemViewModel

@Composable

//가장 기본 스크린
fun HomeScreen(
    onNavigateSubScreen: (String, String, Int) -> Unit,
    itemViewModel: ItemViewModel = viewModel()
) {

    val itemList = itemViewModel.itemList


    Column {
        Column {
            Spacer(Modifier.height(30.dp))
            Text(
                "202112346 정근녕 친구목록",
                fontWeight = FontWeight.Bold, fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
            ItemInput(itemList, itemViewModel)
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    itemViewModel.sortList()

                }
                .align(Alignment.CenterHorizontally)
                .background(Color.Gray), text = "친구목록"
        )
        LazyColumn(
            modifier = Modifier,
            contentPadding = PaddingValues(10.dp),
            //패딩을 줘서 화면 끝에서 잘리지않고 자연스럽게 화면 밖으로 사라지게 만듬
            verticalArrangement = Arrangement.spacedBy(10.dp),
            // 콘텐츠 사이는 10 dp씩 떨어짐
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //item들을 list 바탕으로 만드는데, text는 item의 index로
            itemsIndexed(itemList) { index, item ->
                itemCell(
                    itemData = item,
                    onUserClick = {
                        onNavigateSubScreen(item.name, item.number, item.click)
                    },
                    onHeartClick = { itemViewModel.incrementClick(index) })
            }
        }
    }


}