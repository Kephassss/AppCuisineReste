package com.repasdelaflemme.app.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "recipe_ingredients",
    primaryKeys = ["recipeId", "ingredientId"]
)
data class RecipeIngredientCrossRef(
    val recipeId: Long,
    val ingredientId: Long,
    val quantity: Double? = null,
    val unit: String? = null
)
