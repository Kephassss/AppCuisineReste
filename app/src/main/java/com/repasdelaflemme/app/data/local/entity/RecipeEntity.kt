package com.repasdelaflemme.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val steps: String? = null,
    val imageUrl: String? = null,
    val timeMinutes: Int? = null,
    val servings: Int? = null,
    val difficulty: String? = null
)
