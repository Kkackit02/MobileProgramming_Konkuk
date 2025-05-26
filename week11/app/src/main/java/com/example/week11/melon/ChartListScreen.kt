import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChartListScreen() {
    val owner = LocalViewModelStoreOwner.current
    val viewModel: ChartViewModel = viewModel(
        factory = ChartViewModelFactory(owner)
    )
    val chartList = viewModel.chartList
    val isLoading = viewModel.isLoading.value

    LaunchedEffect(Unit) {
        viewModel.fetchMelonChartFromAssets()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Text("불러오는 중...", modifier = Modifier.align(Alignment.Center))
        } else if (chartList.isEmpty()) {
            Text("데이터 없음", modifier = Modifier.align(Alignment.Center))
        } else {
            Column {
                Text(
                    text = "총 ${chartList.size}곡",
                    modifier = Modifier.padding(8.dp)
                )
                LazyColumn {
                    items(chartList) { item ->
                        ChartItem(item)
                    }
                }
            }
        }
    }
}
