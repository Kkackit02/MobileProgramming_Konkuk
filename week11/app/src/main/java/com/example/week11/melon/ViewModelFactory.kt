import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

class ChartViewModelFactory(
    private val owner: ViewModelStoreOwner?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = checkNotNull(owner).let {
            (it as? androidx.lifecycle.HasDefaultViewModelProviderFactory)?.defaultViewModelCreationExtras
        } ?: throw IllegalArgumentException("Owner is null or invalid")

        return ChartViewModel(Application()) as T
    }
}
