package com.repasdelaflemme.app.ui.common;

import java.util.List;

public class RecipeCard {
    public String id;
    public String title;
    public int minutes;
    public Integer matchScore; // null when not applicable
    public Integer missingCount; // nombre d'ingredients manquants
    public Boolean vegetarian;
    public Boolean halal;
    public Integer budget; // 1,2,3
    public List<String> utensils;
    public String cuisine;
    public List<String> allergens;
    public Boolean containsAlcohol;
    public Boolean containsPork;
    public Integer imageResId; // drawable res id si dispo
    public String imageUrl; // url http(s) si dispo

    public RecipeCard(String id, String title, int minutes, Integer matchScore) {
        this.id = id;
        this.title = title;
        this.minutes = minutes;
        this.matchScore = matchScore;
    }

    public RecipeCard withMissing(Integer missing) { this.missingCount = missing; return this; }
    public RecipeCard withVegetarian(Boolean veg) { this.vegetarian = veg; return this; }
    public RecipeCard withHalal(Boolean h) { this.halal = h; return this; }
    public RecipeCard withBudget(Integer b) { this.budget = b; return this; }
    public RecipeCard withUtensils(List<String> uts) { this.utensils = uts; return this; }
    public RecipeCard withCuisine(String c) { this.cuisine = c; return this; }
    public RecipeCard withAllergens(List<String> a) { this.allergens = a; return this; }
    public RecipeCard withContainsAlcohol(Boolean v) { this.containsAlcohol = v; return this; }
    public RecipeCard withContainsPork(Boolean v) { this.containsPork = v; return this; }
    public RecipeCard withImage(Integer resId) { this.imageResId = resId; return this; }
    public RecipeCard withImageUrl(String url) { this.imageUrl = url; return this; }
}

