package com.example.week14.viewmodel

data class ItemEntity(
    val itemName:String,
    val itemQuantity:Int,
    val itemID:Int
)
{
    constructor():this("noinfo",0,0)
}


