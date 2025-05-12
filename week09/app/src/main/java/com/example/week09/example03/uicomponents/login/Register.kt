package com.example.week09.example03.uicomponents.login

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.week09.example03.uicomponents.LocalNavGraphViewModelStoreOwner
import com.example.week09.example03.viewmodel.NavViewModel

@Composable
fun Register() {
    val navViewModel: NavViewModel = viewModel(viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current)

    var userID by remember{
        mutableStateOf(navViewModel.userID)
    }
    var userPasswd by remember{
        mutableStateOf(navViewModel.userPasswd)
    }

    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Log.d("RegisterViewModel",navViewModel.toString() )
        Text(
            text = "Register",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "${userID}, let's start the registration process.",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )

        OutlinedTextField(value = userID?:"",
            onValueChange = {userID =it},
            label = {Text("User ID")}
        )

        OutlinedTextField( value = userPasswd?:"",
            onValueChange = { userPasswd = it },
            label = { Text("Enter password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

    }
}