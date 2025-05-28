package com.example.teamproject_map_login.viewModel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    fun login(email: String, password: String, onResult: (Boolean, String?, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    onResult(true, uid, null)
                } else {
                    onResult(false, null, it.exception?.message)
                }
            }
    }


    fun register(email: String, password: String, context: Context) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    auth.currentUser?.sendEmailVerification()
                    Toast.makeText(context, "인증 메일을 보냈습니다", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "회원가입 실패: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun checkEmailVerified(onChecked: (Boolean) -> Unit) {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener {
            onChecked(user.isEmailVerified)
        }
    }


}
