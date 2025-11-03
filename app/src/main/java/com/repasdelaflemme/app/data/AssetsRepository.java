package com.repasdelaflemme.app.data;

import android.content.Context;
import android.content.res.AssetManager;
import com.google.gson.Gson;
import com.repasdelaflemme.app.BuildConfig;
import com.google.gson.reflect.TypeToken;
import com.repasdelaflemme.app.data.model.Ingredient;
import com.repasdelaflemme.app.data.model.Recipe;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AssetsRepository {

    private static volatile List<Recipe> RECIPES_CACHE;
    private static volatile List<Ingredient> INGREDIENTS_CACHE;

    public static List<Recipe> getRecipes(Context context) {
        List<Recipe> cached = RECIPES_CACHE;
        if (cached != null) return cached;
        synchronized (AssetsRepository.class) {
            if (RECIPES_CACHE != null) return RECIPES_CACHE;
            List<Recipe> all = new ArrayList<>();
            try {
                AssetManager am = context.getAssets();
                String[] packs = am.list("recipes");
                if (packs != null && packs.length > 0) {
                    Type listType = new TypeToken<ArrayList<Recipe>>(){}.getType();
                    for (String file : packs) {
                        if (file == null || !file.endsWith(".json")) continue;
                        try (InputStream is = am.open("recipes/" + file);
                             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                            List<Recipe> list = new Gson().fromJson(reader, listType);
                            if (list != null) all.addAll(list);
                        } catch (Exception ignored) {}
                    }
                }
                if (all.isEmpty()) {
                    try (InputStream is = am.open("recipes.json");
                         BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        Type listType = new TypeToken<ArrayList<Recipe>>(){}.getType();
                        List<Recipe> list = new Gson().fromJson(reader, listType);
                        if (list != null) all.addAll(list);
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
            // Optional synthetic expansion (debug/dev only)
            if (BuildConfig.GENERATE_SYNTHETIC_RECIPES) {
                final long maxMem = Runtime.getRuntime().maxMemory();
                final int target = (maxMem <= 192L * 1024L * 1024L) ? 120 : 240;
                if (all.size() < target) {
                    int need = target - all.size();
                    try { all.addAll(generateSyntheticRecipes(context, need)); } catch (Exception ignored) {}
                } else if (all.size() > target * 2) {
                    all = new java.util.ArrayList<>(all.subList(0, Math.min(all.size(), target * 2)));
                }
            }

            // De-duplicate by id, then by title to avoid visual duplicates
            java.util.LinkedHashMap<String, Recipe> byId = new java.util.LinkedHashMap<>();
            for (Recipe r : all) {
                if (r != null && r.id != null) byId.put(r.id, r);
            }
            java.util.LinkedHashMap<String, Recipe> byTitle = new java.util.LinkedHashMap<>();
            for (Recipe r : byId.values()) {
                String t = r.title != null ? r.title.trim().toLowerCase(java.util.Locale.ROOT) : "";
                if (!byTitle.containsKey(t)) byTitle.put(t, r);
            }
            all = new java.util.ArrayList<>(byTitle.values());
            RECIPES_CACHE = all;
            return RECIPES_CACHE;
        }
    }

    public static List<Ingredient> getIngredients(Context context) {
        List<Ingredient> cached = INGREDIENTS_CACHE;
        if (cached != null) return cached;
        synchronized (AssetsRepository.class) {
            if (INGREDIENTS_CACHE != null) return INGREDIENTS_CACHE;
            try {
                InputStream is = context.getAssets().open("ingredients.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                Type listType = new TypeToken<ArrayList<Ingredient>>(){}.getType();
                List<Ingredient> list = new Gson().fromJson(reader, listType);
                reader.close();
                INGREDIENTS_CACHE = (list != null ? list : new ArrayList<>());
            } catch (Exception e) {
                INGREDIENTS_CACHE = new ArrayList<>();
            }
            return INGREDIENTS_CACHE;
        }
    }

    public static Recipe findRecipeById(Context context, String id) {
        for (Recipe r : getRecipes(context)) {
            if (r.id != null && r.id.equals(id)) return r;
        }
        return null;
    }

    public static void preloadRecipeImages(Context context) {
        try {
            List<Recipe> recipes = getRecipes(context.getApplicationContext());
            for (Recipe r : recipes) {
                if (r.image == null) continue;
                if (r.image.startsWith("http")) {
                    com.bumptech.glide.Glide.with(context.getApplicationContext())
                            .load(r.image)
                            .preload();
                } else if (r.image.startsWith("res:")) {
                    String resName = r.image.substring(4);
                    int id = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
                    if (id != 0) {
                        com.bumptech.glide.Glide.with(context.getApplicationContext())
                                .load(id)
                                .preload();
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    // --- Synthetic dataset generator to scale to 500+ recipes without huge static assets ---
    private static List<Recipe> generateSyntheticRecipes(Context context, int count) {
        java.util.Random rnd = new java.util.Random(42L);
        String[] cuisines = new String[]{"français","italien","asiatique","indien","maghrébin","mexicain","us","méditerranéen","autres"};
        String[] utensils = new String[]{"poele","casserole","four","wok","cocotte","mixeur"};
        String[][] pools = new String[][]{
                {"poulet","oignon","ail","poivron","riz","huile","sel","poivre"},
                {"pate","tomate","oignon","ail","fromage","huile","sel","poivre"},
                {"lentille","carotte","oignon","bouillon_legume","thym","huile","sel","poivre"},
                {"quinoa","concombre","tomate","feta","citron","huile","sel"},
                {"saumon","citron","herbes_de_provence","huile","sel","poivre"},
                {"oeuf","fromage","oignon","beurre","sel","poivre"}
        };
        List<Recipe> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Recipe r = new Recipe();
            String cu = cuisines[i % cuisines.length];
            String base = (i % 2 == 0) ? "Poêlée" : ((i % 3 == 0) ? "Ragoût" : "Salade");
            r.id = "gen_" + cu.replace('é','e').replace('è','e').replace('à','a') + "_" + i;
            r.title = base + " " + cu;
            // time distribution: 30/50/20
            int bucket = i % 10; // 0-2 quick, 3-7 medium, 8-9 long
            if (bucket <= 2) r.minutes = 10 + rnd.nextInt(9); else if (bucket <= 7) r.minutes = 25 + rnd.nextInt(15); else r.minutes = 45 + rnd.nextInt(25);
            // servings
            r.servings = 2 + rnd.nextInt(3);
            // diet distribution
            r.vegetarian = (i % (100/35 == 0 ? 3 : (100/35))) < 1; // approx 35% by pattern
            if (r.vegetarian == null) r.vegetarian = (i % 20) < 7;
            // halal 60%
            r.halal = (i % (100/60 == 0 ? 2 : (100/60))) < 1 || (r.vegetarian != null && r.vegetarian);
            // budget 50/40/10
            int b = rnd.nextInt(10); r.budget = b < 5 ? 1 : (b < 9 ? 2 : 3);
            // utensils 1-2
            r.utensils = new ArrayList<>(); r.utensils.add(utensils[i % utensils.length]); if (rnd.nextBoolean()) r.utensils.add(utensils[(i+3)%utensils.length]);
            r.cuisine = cu;
            // Cycle through local embedded photos (in res/drawable)
            String[] sampleResNames = new String[]{
                    "photo_pasta",
                    "photo_omelette",
                    "photo_fried_rice",
                    "photo_curry",
                    "photo_salad",
                    "photo_ratatouille",
                    "photo_carbonara",
                    "photo_pad_thai"
            };
            r.image = "res:" + sampleResNames[i % sampleResNames.length];
            r.tags = new ArrayList<>(); r.tags.add("auto"); r.tags.add(cu);
            // ingredients
            r.ingredients = new ArrayList<>();
            String[] pool = pools[i % pools.length];
            int nIng = 6 + rnd.nextInt(3);
            java.util.Set<String> used = new java.util.HashSet<>();
            // Use catalog units when possible for realistic quantities
            java.util.Map<String, String> unitById = new java.util.HashMap<>();
            try {
                for (com.repasdelaflemme.app.data.model.Ingredient ing : getIngredients(context)) {
                    if (ing != null && ing.id != null && ing.unit != null) unitById.put(ing.id, ing.unit);
                }
            } catch (Throwable ignored) {}
            for (int k = 0; k < nIng; k++) {
                com.repasdelaflemme.app.data.model.RecipeIngredient ri = new com.repasdelaflemme.app.data.model.RecipeIngredient();
                String id = pool[rnd.nextInt(pool.length)];
                // avoid duplicates
                int tries = 0; while (used.contains(id) && tries++ < 5) id = pool[rnd.nextInt(pool.length)];
                used.add(id);
                ri.id = id;
                String unit = unitById.get(id);
                if (unit == null) {
                    unit = ("pate".equals(id) || "riz".equals(id) || "fromage".equals(id) || "lentille".equals(id)) ? "g"
                            : ("lait".equals(id) || "lait_coco".equals(id) ? "ml" : "pcs");
                }
                ri.unit = unit;
                ri.qty = computeSyntheticQty(id, unit, (r.servings != null ? r.servings : 2), rnd);
                r.ingredients.add(ri);
            }
            // allergens & flags
            r.allergens = new ArrayList<>();
            for (com.repasdelaflemme.app.data.model.RecipeIngredient ri : r.ingredients) {
                if ("pate".equals(ri.id) || "farine".equals(ri.id) || "pain".equals(ri.id) || "tortilla".equals(ri.id)) addIfAbsent(r.allergens, "gluten");
                if ("fromage".equals(ri.id) || "lait".equals(ri.id) || "creme".equals(ri.id) || "mozzarella".equals(ri.id)) addIfAbsent(r.allergens, "lactose");
                if ("oeuf".equals(ri.id)) addIfAbsent(r.allergens, "oeuf");
            }
            r.containsAlcohol = Boolean.FALSE;
            r.containsPork = Boolean.FALSE;
            // steps (6 detailed)
            r.steps = new ArrayList<>();
            String ust = r.utensils.get(0);
            r.steps.add("Préparer les ingrédients: émincer oignon/ail, tailler les légumes (1–2 cm).");
            if ("four".equals(ust)) {
                r.steps.add("Préchauffer le four à " + (180 + rnd.nextInt(30)) + "°C. Disposer les ingrédients sur une plaque.");
                r.steps.add("Arroser d'huile (1 c.s), saler/poivrer, ajouter épices; mélanger.");
                r.steps.add("Enfourner " + (20 + rnd.nextInt(20)) + " min, mélanger à mi-cuisson.");
            } else if ("wok".equals(ust) || "poele".equals(ust)) {
                r.steps.add("Chauffer 1 c.s d'huile à feu moyen-vif dans la " + ust + ".");
                r.steps.add("Saisir protéines/légumes 3–5 min en remuant; assaisonner (sel/poivre/épices).");
                r.steps.add("Cuire encore " + (5 + rnd.nextInt(6)) + " min à feu moyen; texture fondante/croquante.");
            } else if ("casserole".equals(ust) || "cocotte".equals(ust)) {
                r.steps.add("Suer oignon/ail 2–3 min dans 1 c.s d'huile.");
                r.steps.add("Ajouter ingrédients principaux, couvrir d'eau/bouillon; frémir.");
                r.steps.add("Mijoter " + (15 + rnd.nextInt(20)) + " min; ajuster sel/poivre.");
            } else {
                r.steps.add("Assembler, assaisonner, et laisser reposer 5 min.");
                r.steps.add("Rectifier citron/huile/sel selon goût.");
                r.steps.add("Servir frais/tiède.");
            }
            r.steps.add("Finitions: goûter, ajuster l'assaisonnement; servir immédiatement.");
            out.add(r);
        }
        return out;
    }

    private static void addIfAbsent(List<String> list, String k) {
        if (list == null) return; for (String s : list) { if (k.equals(s)) return; } list.add(k);
    }

    // Generate realistic quantities based on unit, id and servings
    private static double computeSyntheticQty(String id, String unit, int servings, java.util.Random rnd) {
        if (unit == null) unit = "pcs";
        String u = unit.toLowerCase(java.util.Locale.ROOT).trim();
        String ing = id != null ? id.toLowerCase(java.util.Locale.ROOT) : "";

        // helpers
        java.util.function.Function<double[], Double> choose = range -> {
            double min = range[0], max = range[1];
            return min + (max - min) * rnd.nextDouble();
        };
        java.util.function.Function<Double, Double> roundHalf = v -> Math.round(v * 2.0) / 2.0;   // .5 steps
        java.util.function.Function<Double, Double> roundQuarter = v -> Math.round(v * 4.0) / 4.0; // .25 steps

        switch (u) {
            case "g": {
                // Staples by id (per person)
                if (ing.equals("pate") || ing.equals("riz") || ing.equals("semoule") || ing.equals("couscous") ||
                    ing.equals("quinoa") || ing.equals("boulgour") || ing.equals("lentille")) {
                    double per = choose.apply(new double[]{75, 100});
                    return Math.round(per * Math.max(1, servings));
                }
                // Cheeses (per person)
                if (ing.equals("fromage") || ing.equals("mozzarella") || ing.equals("parmesan") || ing.equals("feta")) {
                    double per = choose.apply(new double[]{18, 35});
                    return Math.round(per * Math.max(1, servings));
                }
                // Meats / fish / shrimp (per person)
                if (ing.equals("poulet") || ing.equals("boeuf") || ing.equals("porc") || ing.equals("jambon") ||
                    ing.equals("lardon") || ing.equals("saucisse") || ing.equals("saumon") || ing.equals("thon") || ing.equals("crevette")) {
                    double per = choose.apply(new double[]{100, 150});
                    return Math.round(per * Math.max(1, servings));
                }
                // Leafy / berries / small veg by weight (per person, modest)
                double per = choose.apply(new double[]{40, 90});
                return Math.round(per * Math.max(1, servings));
            }
            case "ml": {
                // Milk / coconut milk per person
                if (ing.equals("lait") || ing.equals("lait_coco") || ing.equals("sauce_tomate")) {
                    double per = choose.apply(new double[]{60, 120});
                    return Math.round(per * Math.max(1, servings));
                }
                // Vinegar often small, treat as total, not per person
                if (ing.equals("vinaigre")) {
                    return Math.round(choose.apply(new double[]{10, 30}));
                }
                // Fallback modest total
                return Math.round(choose.apply(new double[]{50, 150}));
            }
            case "tbsp": {
                double v = choose.apply(new double[]{0.5, 2.5});
                return roundHalf.apply(v);
            }
            case "tsp": {
                double v = choose.apply(new double[]{0.25, 1.5});
                return roundQuarter.apply(v);
            }
            case "cube":
            case "sachet":
            case "tranche": {
                int cnt = 1 + rnd.nextInt(2); // 1-2
                return cnt;
            }
            case "pcs":
            default: {
                // Specific piece logic
                if (ing.equals("oeuf")) {
                    // 1-2 eggs per person
                    int per = 1 + rnd.nextInt(2);
                    return per * Math.max(1, servings);
                }
                if (ing.equals("ail")) {
                    // 1 clove per person (cap to 4)
                    int total = Math.max(1, Math.min(4, servings));
                    return total;
                }
                if (ing.equals("oignon")) {
                    // ~0.5 onion per person (cap typical 1-2)
                    double total = 0.5 * Math.max(1, servings);
                    total = Math.max(0.5, Math.min(2.0, total));
                    return roundHalf.apply(total);
                }
                // Generic pieces: 0.5-1.5 per person, clamp 1..6
                double per = choose.apply(new double[]{0.5, 1.5});
                double total = per * Math.max(1, servings);
                total = Math.max(1.0, Math.min(6.0, total));
                return roundHalf.apply(total);
            }
        }
    }
}
