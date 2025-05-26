package com.example.week12.roomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun InsertItem(itemEntity: ItemEntity)
    @Update
    suspend fun UpdateItem(itemEntity: ItemEntity)
    @Delete
    suspend fun DeleteItem(itemEntity: ItemEntity)

    @Query("select * from ItemTable")
    fun getAllItems():Flow<List<ItemEntity>>

    @Query("select * from ItemTable where itemName like :itemName")
    fun getItems(itemName:String):Flow<List<ItemEntity>>

    @Query("select * from ItemTable order by itemQuantity desc")
    fun getSortedItems(): Flow<List<ItemEntity>>
}