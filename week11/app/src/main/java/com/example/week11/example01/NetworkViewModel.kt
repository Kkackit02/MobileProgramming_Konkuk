package com.example.week11.example01

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NewsViewModel : ViewModel() {

    private val _newsList = mutableStateListOf<NewsData>()
    val newsList = _newsList

    private var _isLoading = mutableStateOf(false)
    val isLoading = _isLoading

    fun fetchNews() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fetchedNews = getNews()
                _newsList.clear()
                _newsList.addAll(fetchedNews)
            } catch (e: Exception) {
                Log.e("error", "fetch 관련 오류 발생", e)
            }finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getNews(): List<NewsData> = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect("https://news.daum.net")
            .userAgent("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Mobile Safari/537.36")
            .referrer("https://www.google.com")
            .timeout(10000)
            .get()

        val headlines = doc.select("ul.list_newsheadline2>li")
        headlines.mapNotNull { li ->
            val a = li.selectFirst("a") ?: return@mapNotNull null
            val title = a.select("strong.tit_txt").text()
            val link = a.absUrl("href")
            NewsData(title.toString(), link)
        }
    }
}