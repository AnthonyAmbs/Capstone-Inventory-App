package com.example.anthonyambscs499inventoryproject

data class InventoryItem(
    val itemId: String? = null,
    val itemName: String,
    val itemQty: Int,
    val itemNotifs: Boolean = false
)
