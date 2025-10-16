package com.repasdelaflemme.app.data.remote;

import java.util.List;

public class MealDbResponse {
    public List<Meal> meals;

    public static class Meal {
        public String idMeal;
        public String strMeal;
        public String strCategory;
        public String strArea;
        public String strInstructions;
        public String strMealThumb; // image URL
        // Ingredients come as strIngredient1..20, quantities strMeasure1..20 (ignored here for brevity)
    }
}

