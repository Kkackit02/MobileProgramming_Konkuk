package com.example.week11.example01

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider

data class NewsData(
    var title: String,
    var newsUrl: String
)

@Composable
fun NewsItem(news: NewsData) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable {
            val intent = Intent(Intent.ACTION_VIEW, news.newsUrl.toUri())
            context.startActivity(intent)
        }
    ) {
        Text(news.title, fontSize = 20.sp)
    }
}

@Composable
fun NewsList(list: List<NewsData>) {
    LazyColumn {
        items(list) { item ->
            NewsItem(item)
            HorizontalDivider(color = Color.Black, thickness = 1.dp)
        }
    }
}
@Composable
fun ChartItem(data: ChartData) {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(text = data.title, fontSize = 18.sp)
        Text(text = data.artist, fontSize = 14.sp)
    }
}

@Composable
fun ChartList(list: List<ChartData>) {
    LazyColumn {
        items(list) { chart ->
            ChartItem(chart)
        }
    }
}