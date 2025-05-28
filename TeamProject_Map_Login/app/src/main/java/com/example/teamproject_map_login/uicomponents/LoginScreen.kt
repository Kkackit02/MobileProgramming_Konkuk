package com.example.teamproject_map_login.uicomponents

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.teamproject_map_login.viewModel.AuthViewModel

@Composable
fun LoginScreen(
    onWelcomeNavigate: (String) -> Unit,
    onRegisterNavigate: (String, String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("CampusPeer", fontSize = 28.sp)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.login(email, password) { success, uid, error ->
                    if (success && uid != null) {
                        onWelcomeNavigate(uid)
                    } else {
                        Toast.makeText(context, error ?: "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log In")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            onRegisterNavigate(email, password)
        }) {
            Text("계정이 없으신가요? Sign Up")
        }
    }
}

