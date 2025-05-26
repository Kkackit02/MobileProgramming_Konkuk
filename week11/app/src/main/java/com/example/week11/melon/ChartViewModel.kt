import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class ChartViewModel(app: Application) : AndroidViewModel(app) {

    private val _chartList = mutableStateListOf<ChartData>()
    val chartList = _chartList

    val isLoading = mutableStateOf(false)

    fun fetchMelonChart() {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val list = getChartFromAsset()
                _chartList.clear()
                _chartList.addAll(list)
                Log.d("melon", "크롤링 성공 - ${list.size}개")
            } catch (e: Exception) {
                Log.e("melon", "크롤링 오류", e)
            } finally {
                isLoading.value = false
            }
        }
    }
    private suspend fun getChartFromAssets(): List<ChartData> = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()
        val inputStream = context.assets.open("chart.html")
        val doc = Jsoup.parse(inputStream, "UTF-8", "")

        val rows = doc.select("tr.lst100")
        Log.d("melon", "총 행 개수: ${rows.size}")

        return@withContext rows.mapNotNull { row ->
            val titleElement = row.selectFirst("div.ellipsis.rank01 > span > a")
            val artistElement = row.selectFirst("div.ellipsis.rank02 > a")

            val title = titleElement?.text()
            val artist = artistElement?.text()

            if (title != null && artist != null) {
                ChartData(title, artist)
            } else null
        }
    }

    fun fetchMelonChartFromAssets() {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val list = getChartFromAssets()
                _chartList.clear()
                _chartList.addAll(list)
                Log.d("melon", "에셋 기반 파싱 성공 - ${list.size}개")
            } catch (e: Exception) {
                Log.e("melon", "에셋 크롤링 오류", e)
            } finally {
                isLoading.value = false
            }
        }
    }

}
