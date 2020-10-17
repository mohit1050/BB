package com.example.bb_items_roomdb.RoomDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// this class contains column info
@Entity
class ItemEntity {

    @PrimaryKey(autoGenerate = true)
    var itemId: Int = 0

    @ColumnInfo(name = "ItemName") // new column / colum name
    var itemName: String = ""

    @ColumnInfo(name = "Quantity")
    var quantity: String = ""

    @ColumnInfo(name = "GST")
    var gst: String = ""

    @ColumnInfo(name = "Amount")
    var amount: String = ""

    @ColumnInfo(name = "Sub_Total")
    var subTotal: String = ""

    @ColumnInfo(name = "Tax_total")
    var taxTotal: String = ""

}
// note : make itemms save in above table as listed below

// sub_total = amount * quantity
// tax_total = quantity * tax
// total = $sub_total + $tax_total