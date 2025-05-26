package com.example.week12.roomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ItemTable")
data class ItemEntity(
    val itemName:String,
    val itemQuantity:Int,
    @PrimaryKey(autoGenerate = true)
    val itemID:Int=0
)