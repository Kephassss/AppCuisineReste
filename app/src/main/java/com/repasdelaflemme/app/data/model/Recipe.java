package com.repasdelaflemme.app.data.model;

import java.util.List;

public class Recipe {
    public String id;
    public String title;
    public int minutes;
    public Integer servings;
    public Boolean vegetarian;
    public Boolean halal;          // optional flag
    public Integer budget;         // optional, e.g., 1=€ (pas cher) .. 3=€€€
    public List<String> utensils;  // optional, ex: ["poele","four","casserole","wok"]
    public String cuisine;         // optional, ex: "italien", "asiatique", "français"
    public List<String> allergens; // optional, ex: ["gluten","lactose","oeuf","arachide","fruits_a_coque","crustace","soja","sesame"]
    public Boolean containsAlcohol; // optional, true si alcool présent
    public Boolean containsPork;    // optional, true si porc présent
    public String image; // optional, e.g., "res:ic_recipe"
    public List<String> tags;
    public List<RecipeIngredient> ingredients;
    public List<String> steps;
}
