package com.example.week09.example02.uicomponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.week09.R

@Composable
fun ColumnScope.DrawerContent(){

    /*
    NavigationDrawerItem(
        modifier = Modifier.fillMaxWidth(),
        label =  {Text(text = "Image")},
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.image1), // 이미지 리소스 ID
                contentDescription = "Image icon"
            )
        },
        onClick = {},
        selected = false
    )
    */

    Image(
        painter = painterResource(id = R.drawable.image1), // 이미지 리소스 ID
        contentDescription = "Image icon",
        modifier = Modifier.fillMaxWidth()
    )
    Text(text = "202112346 정근녕",
        modifier = Modifier.padding(16.dp),
        fontSize = 20.sp
        )
    Text(text = "jgn5493@gmail.com",
        modifier = Modifier.padding(10.dp),
        fontSize = 20.sp
    )
    NavigationDrawerItem(
        modifier = Modifier.fillMaxWidth(),
        label =  {Text(text = "Drawer Item 1")},
        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "") },
        onClick = {},
        selected = false
    )
    NavigationDrawerItem(
        modifier = Modifier.fillMaxWidth(),
        label =  {Text(text = "Drawer Item 2")},
        onClick = {},
        selected = false
    )

}