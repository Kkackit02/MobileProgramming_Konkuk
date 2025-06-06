package com.example.week09.example01.uicomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.week09.example01.viewmodel.NavViewModel

@Composable
fun WelcomeScreen() {

    val navViewModel: NavViewModel = viewModel(viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current)

    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Welcome Screen",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "Welcome ${navViewModel.userID}",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}