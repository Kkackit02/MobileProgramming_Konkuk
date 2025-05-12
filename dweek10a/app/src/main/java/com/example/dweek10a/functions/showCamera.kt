package com.example.dweek10a.functions

import android.content.Context
import android.content.Intent
import android.provider.MediaStore

fun showCamera(context:Context){
    val cameraIntent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    context.startActivity(cameraIntent)

}