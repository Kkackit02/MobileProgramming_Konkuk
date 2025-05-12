package com.example.week10.functions

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun makeCall(context:Context){
    val number = "tel:010-1234-1234".toUri()
    val callIntent= Intent(Intent.ACTION_CALL, number)
    context.startActivity(callIntent)

}