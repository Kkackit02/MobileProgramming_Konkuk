import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChartItem(item: ChartData) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .border(1.dp, Color.Gray)
            .padding(10.dp)
    ) {
        Text(text = item.title, fontSize = 18.sp)
        Text(text = item.artist, fontSize = 14.sp, color = Color.DarkGray)
    }
}
