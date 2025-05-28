package com.example.teamproject_map_login.uicomponents

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
fun RegisterScreen(
    onLoginNavigate: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    // 모든 필수 항목이 채워졌는지 검사
    val allInputsFilled = email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            department.isNotBlank() &&
            studentId.isNotBlank() &&
            selectedImageUri != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("CampusPeer", fontSize = 28.sp)
        Text("Sign Up", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("비밀번호 확인") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { galleryLauncher.launch("image/*") }) {
                Text("학생증 업로드")
            }
            Text(
                text = selectedImageUri?.lastPathSegment ?: "파일 없음",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = department,
            onValueChange = { department = it },
            label = { Text("학과  예 : 컴퓨터공학부") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = studentId,
            onValueChange = { studentId = it },
            label = { Text("학번  예 : 202512345") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    Toast.makeText(context, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                viewModel.register(email, password, context)

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("admin@yourdomain.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "CampusPeer 가입 승인 요청")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "이메일: $email\n학과: $department\n학번: $studentId\n비밀번호: $password"
                    )
                    putExtra(Intent.EXTRA_STREAM, selectedImageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "메일 앱 선택"))
            },
            enabled = allInputsFilled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("승인 요청")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            Text("이미 계정이 있으신가요?")
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(onClick = onLoginNavigate) {
                Text("Log In")
            }
        }
    }
}
