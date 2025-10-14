package com.repasdelaflemme.app.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.repasdelaflemme.app.data.local.entity.RecipeEntity
import com.repasdelaflemme.app.data.local.entity.RecipeIngredientCrossRef
import com.repasdelaflemme.app.data.local.model.RecipeWithIngredients

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>): List<Long>

    @Update
    suspend fun update(recipe: RecipeEntity)

    @Delete
    suspend fun delete(recipe: RecipeEntity)

    @Query("SELECT * FROM recipes ORDER BY title ASC")
    fun observeAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: Long): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipeIngredientLinks(links: List<RecipeIngredientCrossRef>)

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getWithIngredients(id: Long): RecipeWithIngredients?

    @Transaction
    @Query("""
        SELECT * FROM recipes
        WHERE title LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun searchWithIngredients(query: String): Flow<List<RecipeWithIngredients>>
}
