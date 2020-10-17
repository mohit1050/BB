package com.example.bb_items_roomdb.RoomDB

import androidx.room.*

// database file
@Database(entities = [(ItemEntity::class)], version = 1)
abstract class AppDb : RoomDatabase() {

    abstract fun abstractItemDao(): ItemDAO
}