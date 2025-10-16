package com.repasdelaflemme.app.ui.home;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.repasdelaflemme.app.R;
import com.repasdelaflemme.app.data.AssetsRepository;
import com.repasdelaflemme.app.data.PrefPantryStore;
import com.repasdelaflemme.app.data.model.Recipe;
import com.repasdelaflemme.app.data.model.RecipeIngredient;
import com.repasdelaflemme.app.ui.common.RecipeCard;
import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<RecipeCard>> recipeCards = new MutableLiveData<>();

    public LiveData<List<RecipeCard>> getRecipeCards() {
        return recipeCards;
    }

    public void loadRecipes(Context context) {
        // Idéalement, ce chargement devrait se faire dans un thread différent,
        // mais pour garder la logique simple, nous la laissons ici.
        List<Recipe> recipes = AssetsRepository.getRecipes(context);
        if (recipes.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.error_loading_recipes), Toast.LENGTH_SHORT).show();
        }
        recipeCards.setValue(toCards(recipes, context));
    }

    private List<RecipeCard> toCards(List<Recipe> recipes, Context context) {
        PrefPantryStore store = new PrefPantryStore(context);
        List<String> have = store.getIngredientIds();
        List<RecipeCard> out = new ArrayList<>();
        Resources res = context.getResources();
        String packageName = context.getPackageName();

        for (Recipe r : recipes) {
            int haveCount = 0;
            int total = (r.ingredients != null) ? r.ingredients.size() : 0;

            if (total > 0) {
                for (RecipeIngredient ri : r.ingredients) {
                    if (have.contains(ri.id)) {
                        haveCount++;
                    }
                }
            }

            Integer resId = null;
            String imageUrl = null;
            if (r.image != null) {
                if (r.image.startsWith("res:")) {
                    String name = r.image.substring(4);
                    int id = res.getIdentifier(name, "drawable", packageName);
                    if (id != 0) resId = id;
                } else if (r.image.startsWith("http")) {
                    imageUrl = r.image;
                }
            }

            RecipeCard card = new RecipeCard(
                    r.id,
                    r.title,
                    r.minutes,
                    total == 0 ? null : (haveCount * 100 / Math.max(1, total))
            )
                    .withMissing(Math.max(0, total - haveCount))
                    .withVegetarian(r.vegetarian != null && r.vegetarian)
                    .withHalal(r.halal)
                    .withBudget(r.budget)
                    .withUtensils(r.utensils)
                    .withCuisine(r.cuisine)
                    .withAllergens(r.allergens)
                    .withContainsAlcohol(r.containsAlcohol)
                    .withContainsPork(r.containsPork)
                    .withImage(resId)
                    .withImageUrl(imageUrl);

            out.add(card);
        }
        return out;
    }
}
