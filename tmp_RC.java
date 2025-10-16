package com.repasdelaflemme.app.ui.common;

public class RecipeCard {
    public String id;
    public String title;
    public int minutes;
    public Integer matchScore; // null when not applicable
    public Integer missingCount; // nombre d'ingrÃ©dients manquants
    public Boolean vegetarian;
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
    public RecipeCard withImage(Integer resId) { this.imageResId = resId; return this; }
    public RecipeCard withImageUrl(String url) { this.imageUrl = url; return this; }
}

