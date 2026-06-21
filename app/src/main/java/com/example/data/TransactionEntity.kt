package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val category: String,
    val date: String,
    val note: String,
    val isIncome: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
