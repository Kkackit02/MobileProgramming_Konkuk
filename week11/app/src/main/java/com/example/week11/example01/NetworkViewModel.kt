package com.example.week11.example01

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

class NewsViewModel : ViewModel() {

    private val _newsList = mutableStateListOf<NewsData>()
    val newsList = _newsList
    private val _chartList = mutableStateListOf<ChartData>()
    val chartList = _chartList
    private val _isLoading = mutableStateOf(false)

    val isLoading = _isLoading

    fun fetchMelonChart() {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val list = getMelonChart()
                _chartList.clear()
                _chartList.addAll(list)
                Log.d("bugs", "크롤링 성공 - ${list.size}개")
            } catch (e: Exception) {
                Log.e("bugs", "크롤링 오류", e)
            } finally {
                isLoading.value = false
            }
        }
    }
    fun fetchNews() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                //val fetchedNews = getNews()
                val fetchedNews = getJtbcNews()
                _newsList.clear()
                _newsList.addAll(fetchedNews)
                val joke = getJoke()
                Log.i(joke,"joke")
            } catch (e: Exception) {
                Log.e("error", "fetch 관련 오류 발생", e)
            }

            finally {
                _isLoading.value = false
            }
        }
    }
    private suspend fun getMelonChart(): List<ChartData> = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect("https://www.melon.com/chart/index.htm")
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
            .referrer("https://www.google.com")
            .timeout(10000)
            .get()

        val rows = doc.select("tr.lst50, tr.lst100") // 상위 100위: lst50 + lst100

        rows.mapNotNull { row ->
            val titleElement = row.selectFirst("div.ellipsis.rank01 > span > a")
            val artistElement = row.selectFirst("div.ellipsis.rank02 > a")

            val title = titleElement?.text()?.trim()
            val artist = artistElement?.text()?.trim()

            if (!title.isNullOrBlank() && !artist.isNullOrBlank()) {
                ChartData(title, artist)
            } else null
        }
    }


    private suspend fun getBugsChart(): List<ChartData> = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect("https://music.bugs.co.kr/chart")
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
            .referrer("https://www.google.com")
            .timeout(10000)
            .get()

        val rows = doc.select("table.list.trackList > tbody > tr")
        rows.mapNotNull { row ->
            val titleElement = row.selectFirst("p.title > a")
            val artistElement = row.selectFirst("p.artist > a")

            val title = titleElement?.text()?.trim()
            val artist = artistElement?.text()?.trim()

            if (!title.isNullOrBlank() && !artist.isNullOrBlank()) {
                ChartData(title, artist)
            } else null
        }
    }


    private suspend fun getJoke(): String = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect("https:/api.chucknorris.io/kokes/random?categories=food")
            .ignoreContentType(true)
            .get()
        val json = JSONObject(doc.text())
        val joke = json.getString("value")
        joke
    }

    private suspend fun getJtbcNews(): List<NewsData> = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect("https://fs.jtbc.co.kr/RSS/culture.xml")
            .parser(Parser.xmlParser()).get()
        val headlines = doc.select("item")
        headlines.mapNotNull {  news ->
            NewsData(news.selectFirst("title")?.text().toString(),
                news.select("link")?.text().toString())
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