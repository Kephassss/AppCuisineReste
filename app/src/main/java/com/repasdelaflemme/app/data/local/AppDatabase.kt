package com.repasdelaflemme.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.repasdelaflemme.app.data.local.dao.IngredientDao
import com.repasdelaflemme.app.data.local.dao.RecipeDao
import com.repasdelaflemme.app.data.local.entity.*

@Database(
    entities = [
        IngredientEntity::class,
        RecipeEntity::class,
        RecipeIngredientCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
}
