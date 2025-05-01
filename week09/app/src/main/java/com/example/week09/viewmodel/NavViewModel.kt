package com.example.week09.viewmodel

import androidx.lifecycle.ViewModel

class NavViewModel : ViewModel(){
    val userid = "greenjoa"
    val userpasswd = "1234"

    var userID:String? = null
    var userPasswd:String? = null

    fun checkInfo(id:String, passwd:String):Boolean{
        setUserInfo(id,passwd)
        return (userid == id && userpasswd == passwd)
    }

    fun setUserInfo(id:String, passwd:String){
        userID = id
        userPasswd = passwd
    }
}
