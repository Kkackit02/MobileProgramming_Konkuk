package com.example.week09.example03.uicomponents.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
fun LoginScreen(onWelcomeNavigate: () -> Unit, onRegisterNavigate: () -> Unit) {

    val navViewModel: NavViewModel = viewModel(viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current)

    var userID by remember {
        mutableStateOf("greenjoa")
    }

    var userPasswd by remember {
        mutableStateOf("1234")
    }

    var loginresult = navViewModel.checkInfo(userID, userPasswd)

    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Text(text="Home Screen",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold)

        OutlinedTextField(value = userID,
            onValueChange = {userID =it},
            label = {Text("User ID")}
        )

        OutlinedTextField( value = userPasswd,
            onValueChange = { userPasswd = it },
            label = { Text("Enter password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Button(onClick = {

            navViewModel.setUserInfo(userID, userPasswd)

            if(loginresult) {
                onWelcomeNavigate()
            }
            else {
                onRegisterNavigate()
            }

        }){
            Text(text = "Login")
        }
    }
}