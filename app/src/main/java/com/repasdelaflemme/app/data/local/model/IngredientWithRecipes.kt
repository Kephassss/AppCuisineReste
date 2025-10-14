package com.repasdelaflemme.app.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.repasdelaflemme.app.data.local.entity.*

data class IngredientWithRecipes(
    @Embedded val ingredient: IngredientEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RecipeIngredientCrossRef::class,
            parentColumn = "ingredientId",
            entityColumn = "recipeId"
        )
    )
    val recipes: List<RecipeEntity>
)
