package com.example.dweek05a.uicomponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun ButtonWithBadge(likes: Int, onClick: () -> Unit) {
    Column(modifier = Modifier.padding(8.dp)) {
        BadgedBox(badge = { Badge { Text("$likes") } }) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = if (likes > 0) Color.Red else LocalContentColor.current,
                //icon의 색은 color이 아니라 tint를 바꾸는 것
                modifier = Modifier.clickable { onClick() }
                //click 이벤트가 없는데
                //click을 추가하기 위해 modifier = Modifier.clickable {onClick()} 활용

            )
        }
    }
}

@Preview
@Composable
private fun ButtonWithBadgePreview() {
    var likes by remember { mutableIntStateOf(0) }
    ButtonWithBadge(likes = likes) { likes++ }
}