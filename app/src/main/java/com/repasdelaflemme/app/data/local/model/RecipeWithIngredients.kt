package com.repasdelaflemme.app.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.repasdelaflemme.app.data.local.entity.*

data class RecipeWithIngredients(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RecipeIngredientCrossRef::class,
            parentColumn = "recipeId",
            entityColumn = "ingredientId"
        )
    )
    val ingredients: List<IngredientEntity>
)
