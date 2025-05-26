package com.example.week11.example01

// 설치 라이브러리
// org.jsoup:jsoup
// androidx.compose.material:material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh

import androidx.compose.material.pullrefresh.rememberPullRefreshState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FetchDaumNews(newsViewModel: NewsViewModel = viewModel()) {
    val newsList = newsViewModel.newsList
    val isLoading = newsViewModel.isLoading.value
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {newsViewModel.fetchNews()}
    )
    LaunchedEffect(Unit) {
        newsViewModel.fetchNews()
    }



    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pullRefresh((pullRefreshState))
    ) {
        NewsList(list = newsList)
        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

    }
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FetchBugsChart(newsViewModel: NewsViewModel = viewModel()) {
    val chartList = newsViewModel.chartList
    val isLoading = newsViewModel.isLoading.value
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { newsViewModel.fetchBugsChart() }
    )

    LaunchedEffect(Unit) {
        newsViewModel.fetchBugsChart()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pullRefresh(pullRefreshState)
    ) {
        ChartList(list = chartList)
        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
