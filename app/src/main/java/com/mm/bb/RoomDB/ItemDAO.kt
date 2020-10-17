package com.example.bb_items_roomdb.RoomDB

import androidx.room.*

// this class contains queries
@Dao
interface ItemDAO {
    @Insert
    fun saveItems(item: ItemEntity) // add item

    @Update
    fun updateItem(item: ItemEntity) // update item

    @Delete
    fun deleteItem(item: ItemEntity) // delete single item

    @Query("DELETE from ItemEntity")
    fun deleteAllItems() // delete all item

    @Query("Select * from ItemEntity")
    fun getAllItems(): List<ItemEntity> // list all item

    @Query("select  SUM(Sub_Total) from ItemEntity ")
    fun getSubTotal(): Int // returns sum of _ column

    @Query("select  SUM(Tax_total) from ItemEntity ")
    fun getTaxTotal(): Int // returns sum of _ column

}