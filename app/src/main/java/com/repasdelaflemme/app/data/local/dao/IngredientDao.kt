package com.repasdelaflemme.app.data.local.dao

import androidx.room.*
import com.repasdelaflemme.app.data.local.entity.IngredientEntity
import com.repasdelaflemme.app.data.local.model.IngredientWithRecipes
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingredient: IngredientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ingredients: List<IngredientEntity>): List<Long>

    @Update
    suspend fun update(ingredient: IngredientEntity)

    @Delete
    suspend fun delete(ingredient: IngredientEntity)

    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun observeAll(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getById(id: Long): IngredientEntity?

    @Transaction
    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getWithRecipes(id: Long): IngredientWithRecipes?
}
