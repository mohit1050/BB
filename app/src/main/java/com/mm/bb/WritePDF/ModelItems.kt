package me.kariot.pdfinvoice

data class ModelItems(
    val itemName: String,
    val quantity: String,
    val gst: String,
    val netAmount: String
)