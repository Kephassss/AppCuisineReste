package com.repasdelaflemme.app.ui.recipes;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.chip.Chip;
import android.widget.EditText;
import android.widget.TextView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.repasdelaflemme.app.R;
import com.repasdelaflemme.app.data.AssetsRepository;
import com.repasdelaflemme.app.data.PrefPantryStore;
import com.repasdelaflemme.app.data.model.Recipe;
import com.repasdelaflemme.app.data.model.RecipeIngredient;
import com.repasdelaflemme.app.ui.common.RecipeCard;
import com.repasdelaflemme.app.ui.home.RecipeAdapter;
import android.view.animation.AnimationUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.repasdelaflemme.app.data.remote.MealDbClient;
import com.repasdelaflemme.app.data.remote.MealDbResponse;

public class RecipesFragment extends Fragment { private androidx.recyclerview.widget.RecyclerView listView;

    private RecipeAdapter adapter;
    private List<Recipe> allRecipes = new ArrayList<>();
    private MealDbClient remote;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.rootView = view;

        RecyclerView list = view.findViewById(R.id.recipesList);
        View empty = view.findViewById(R.id.emptyView);
        list.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), 2));
        // Show skeleton while initial filters compute
        list.setAdapter(new com.repasdelaflemme.app.ui.home.SkeletonAdapter(8));
        adapter = new RecipeAdapter(item -> {
            NavController nav = NavHostFragment.findNavController(this);
            Bundle args = new Bundle();
            args.putString("recipeId", item.id);
            nav.navigate(R.id.recipeDetailFragment, args);
        });
        list.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_slide_up));
        this.listView = list;

        allRecipes = AssetsRepository.getRecipes(requireContext());
        remote = new MealDbClient();

        EditText search = view.findViewById(R.id.inputSearch);
        Chip veg = view.findViewById(R.id.checkVegetarian);
        Chip quick = view.findViewById(R.id.checkQuick);
        Chip fromPantryOnly = view.findViewById(R.id.checkFromPantryOnly);
        Chip allowOneMissing = view.findViewById(R.id.checkAllowOneMissing);
        Chip halal = view.findViewById(R.id.checkHalal);
        Chip b1 = view.findViewById(R.id.checkBudget1);
        Chip b2 = view.findViewById(R.id.checkBudget2);
        Chip b3 = view.findViewById(R.id.checkBudget3);
        Chip tQuick = view.findViewById(R.id.checkTimeQuick);
        Chip tMed = view.findViewById(R.id.checkTimeMedium);
        Chip tLong = view.findViewById(R.id.checkTimeLong);
        Chip uPoele = view.findViewById(R.id.checkUstPoele);
        Chip uCasserole = view.findViewById(R.id.checkUstCasserole);
        Chip uFour = view.findViewById(R.id.checkUstFour);
        Chip uWok = view.findViewById(R.id.checkUstWok);
        Chip uCocotte = view.findViewById(R.id.checkUstCocotte);
        Chip uMixeur = view.findViewById(R.id.checkUstMixeur);
        Chip cFr = view.findViewById(R.id.checkCuisineFrancais);
        Chip cIt = view.findViewById(R.id.checkCuisineItalien);
        Chip cAs = view.findViewById(R.id.checkCuisineAsiatique);
        Chip cIn = view.findViewById(R.id.checkCuisineIndien);
        Chip cMagh = view.findViewById(R.id.checkCuisineMaghreb);
        Chip cMex = view.findViewById(R.id.checkCuisineMexicain);
        Chip cUs = view.findViewById(R.id.checkCuisineUS);
        Chip cMed = view.findViewById(R.id.checkCuisineMed);
        Chip cOther = view.findViewById(R.id.checkCuisineAutres);
        Chip aNoGluten = view.findViewById(R.id.checkNoGluten);
        Chip aNoLactose = view.findViewById(R.id.checkNoLactose);
        Chip aNoEgg = view.findViewById(R.id.checkNoOeuf);
        Chip aNoPeanut = view.findViewById(R.id.checkNoArachide);
        Chip aNoTreeNuts = view.findViewById(R.id.checkNoFruitsCoque);
        Chip aNoShell = view.findViewById(R.id.checkNoCrustace);
        Chip aNoSoy = view.findViewById(R.id.checkNoSoja);
        Chip aNoSesame = view.findViewById(R.id.checkNoSesame);
        Chip aNoAlcohol = view.findViewById(R.id.checkNoAlcohol);
        Chip aNoPork = view.findViewById(R.id.checkNoPork);

        View.OnClickListener trigger = v -> {
            try { applyFilters(search, veg, quick, fromPantryOnly, allowOneMissing); }
            catch (Throwable t) { Log.w("RecipesFragment", "applyFilters crashed", t); showSafeError(); }
        };
        if (veg != null) veg.setOnClickListener(trigger);
        if (quick != null) quick.setOnClickListener(trigger);
        if (fromPantryOnly != null) fromPantryOnly.setOnClickListener(trigger);
        if (allowOneMissing != null) allowOneMissing.setOnClickListener(trigger);
        for (Chip ch : new Chip[]{halal,b1,b2,b3,tQuick,tMed,tLong,uPoele,uCasserole,uFour,uWok,uCocotte,uMixeur,cFr,cIt,cAs,cIn,cMagh,cMex,cUs,cMed,cOther,aNoGluten,aNoLactose,aNoEgg,aNoPeanut,aNoTreeNuts,aNoShell,aNoSoy,aNoSesame,aNoAlcohol,aNoPork}) {
            if (ch != null) ch.setOnClickListener(trigger);
        }
        if (search != null) {
            search.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try { applyFilters(search, veg, quick, fromPantryOnly, allowOneMissing); }
                    catch (Throwable t) { Log.w("RecipesFragment", "applyFilters crashed", t); showSafeError(); }
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        boolean focusPantry = getArguments() != null && getArguments().getBoolean("focusPantry", false);
        if (focusPantry && fromPantryOnly != null) {
            fromPantryOnly.setChecked(true);
        }
        // initial
        try { applyFilters(search, veg, quick, fromPantryOnly, allowOneMissing); }
        catch (Throwable t) { Log.w("RecipesFragment", "initial applyFilters crashed", t); showSafeError(); }
    }

    private void applyFilters(EditText search, Chip veg, Chip quick, Chip fromPantryOnly, Chip allowOneMissing) {
        if (getContext() == null) return;
        String q = "";
        try {
            if (search != null && search.getText() != null) {
                q = search.getText().toString().trim().toLowerCase(Locale.ROOT);
            }
        } catch (Throwable ignored) { q = ""; }
        boolean onlyVeg = veg != null && safeIsChecked(veg);
        boolean onlyQuick = quick != null && safeIsChecked(quick);
        boolean pantryOnly = fromPantryOnly != null && safeIsChecked(fromPantryOnly);
        boolean oneMissing = allowOneMissing != null && safeIsChecked(allowOneMissing);
        // collect advanced filters
        boolean halalOnly = chipChecked(R.id.checkHalal);
        java.util.Set<Integer> budgets = new java.util.HashSet<>();
        if (chipChecked(R.id.checkBudget1)) budgets.add(1);
        if (chipChecked(R.id.checkBudget2)) budgets.add(2);
        if (chipChecked(R.id.checkBudget3)) budgets.add(3);
        java.util.Set<String> times = new java.util.HashSet<>(); // quick/med/long markers
        if (chipChecked(R.id.checkTimeQuick)) times.add("quick");
        if (chipChecked(R.id.checkTimeMedium)) times.add("medium");
        if (chipChecked(R.id.checkTimeLong)) times.add("long");
        java.util.Set<String> utensils = new java.util.HashSet<>();
        if (chipChecked(R.id.checkUstPoele)) utensils.add("poele");
        if (chipChecked(R.id.checkUstCasserole)) utensils.add("casserole");
        if (chipChecked(R.id.checkUstFour)) utensils.add("four");
        if (chipChecked(R.id.checkUstWok)) utensils.add("wok");
        if (chipChecked(R.id.checkUstCocotte)) utensils.add("cocotte");
        if (chipChecked(R.id.checkUstMixeur)) utensils.add("mixeur");
        java.util.Set<String> cuisines = new java.util.HashSet<>();
        if (chipChecked(R.id.checkCuisineFrancais)) cuisines.add("franÃ§ais");
        if (chipChecked(R.id.checkCuisineItalien)) cuisines.add("italien");
        if (chipChecked(R.id.checkCuisineAsiatique)) cuisines.add("asiatique");
        if (chipChecked(R.id.checkCuisineIndien)) cuisines.add("indien");
        if (chipChecked(R.id.checkCuisineMaghreb)) cuisines.add("maghrÃ©bin");
        if (chipChecked(R.id.checkCuisineMexicain)) cuisines.add("mexicain");
        if (chipChecked(R.id.checkCuisineUS)) cuisines.add("us");
        if (chipChecked(R.id.checkCuisineMed)) cuisines.add("mÃ©diterranÃ©en");
        if (chipChecked(R.id.checkCuisineAutres)) cuisines.add("autres");
        java.util.Set<String> avoid = new java.util.HashSet<>();
        if (chipChecked(R.id.checkNoGluten)) avoid.add("gluten");
        if (chipChecked(R.id.checkNoLactose)) avoid.add("lactose");
        if (chipChecked(R.id.checkNoOeuf)) avoid.add("oeuf");
        if (chipChecked(R.id.checkNoArachide)) avoid.add("arachide");
        if (chipChecked(R.id.checkNoFruitsCoque)) avoid.add("fruits_a_coque");
        if (chipChecked(R.id.checkNoCrustace)) avoid.add("crustace");
        if (chipChecked(R.id.checkNoSoja)) avoid.add("soja");
        if (chipChecked(R.id.checkNoSesame)) avoid.add("sesame");
        boolean noAlcohol = chipChecked(R.id.checkNoAlcohol);
        boolean noPork = chipChecked(R.id.checkNoPork);

        List<String> have;
        try {
            PrefPantryStore store = new PrefPantryStore(requireContext());
            have = store.getIngredientIds();
            if (have == null) have = new ArrayList<>();
        } catch (Throwable t) {
            have = new ArrayList<>();
        }

        List<RecipeCard> out = new ArrayList<>();
        for (Recipe r : allRecipes) {
            if (onlyVeg && (r.vegetarian == null || !r.vegetarian)) continue;
            if (onlyQuick && r.minutes > 20) continue;
            if (halalOnly && (r.halal == null || !r.halal)) continue;
            if (!budgets.isEmpty()) {
                Integer b = r.budget;
                if (b == null || !budgets.contains(b)) continue;
            }
            if (!times.isEmpty()) {
                boolean okTime = (r.minutes < 20 && times.contains("quick")) ||
                                 (r.minutes >= 20 && r.minutes <= 40 && times.contains("medium")) ||
                                 (r.minutes > 40 && times.contains("long"));
                if (!okTime) continue;
            }
            if (!utensils.isEmpty()) {
                List<String> uts = r.utensils;
                boolean okU = false;
                if (uts != null) {
                    for (String u : uts) { if (utensils.contains(u)) { okU = true; break; } }
                }
                if (!okU) continue;
            }
            if (!cuisines.isEmpty()) {
                String cu = r.cuisine != null ? r.cuisine.toLowerCase(java.util.Locale.ROOT) : null;
                if (cu == null || !cuisines.contains(cu)) continue;
            }
            if (!avoid.isEmpty()) {
                List<String> alls = r.allergens;
                if (alls != null) {
                    boolean hasBad = false;
                    for (String a : alls) { if (avoid.contains(a)) { hasBad = true; break; } }
                    if (hasBad) continue;
                }
            }
            if (noAlcohol && Boolean.TRUE.equals(r.containsAlcohol)) continue;
            if (noPork && Boolean.TRUE.equals(r.containsPork)) continue;

            String hay = (r.title != null ? r.title.toLowerCase(Locale.ROOT) : "");
            if (!q.isEmpty() && !hay.contains(q)) continue;

            int haveCount = 0;
            int total = r.ingredients != null ? r.ingredients.size() : 0;
            if (r.ingredients != null) {
                for (RecipeIngredient ri : r.ingredients) {
                    if (have.contains(ri.id)) haveCount++;
                }
            }
            int missing = Math.max(0, total - haveCount);
            if (pantryOnly && missing > 0) continue;
            if (!pantryOnly && oneMissing && missing > 1) continue;
            Integer resId = null; String imageUrl = null;
            if (r.image != null) {
                if (r.image.startsWith("res:")) {
                    String name = r.image.substring(4);
                    int id = getResources().getIdentifier(name, "drawable", requireContext().getPackageName());
                    if (id != 0) resId = id;
                } else if (r.image.startsWith("http")) {
                    imageUrl = r.image;
                }
            }
            RecipeCard card = new RecipeCard(r.id, r.title, r.minutes, total == 0 ? 0 : (haveCount * 100 / Math.max(1, total)))
                    .withMissing(missing)
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
        // naive sort by matchScore desc
        out.sort((a,b) -> Integer.compare(b.matchScore != null ? b.matchScore : 0, a.matchScore != null ? a.matchScore : 0));
        if (listView != null && listView.getAdapter() != adapter) {
            try { listView.setAdapter(adapter); } catch (Exception ignored) {}
        }
        if (adapter != null) adapter.submit(out);
        try {
            View emptyV = rootView != null ? rootView.findViewById(R.id.emptyView) : null;
            if (emptyV != null) emptyV.setVisibility(out.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (Exception ignored) {}
        
        if (q.length() >= 3 && remote != null) {
            try { remote.search(q, new MealDbClient.OnResult() {
                @Override public void onSuccess(MealDbResponse res) {
                    if (res == null || res.meals == null) return;
                    List<RecipeCard> remoteCards = new ArrayList<>();
                    for (MealDbResponse.Meal m : res.meals) {
                        RecipeCard c = new RecipeCard("mealdb_" + m.idMeal, m.strMeal != null ? m.strMeal : "Recette", 20, null)
                                .withImageUrl(m.strMealThumb);
                        remoteCards.add(c);
                    }
                    List<RecipeCard> combined = new ArrayList<>(out);
                    combined.addAll(remoteCards);
                    if (getActivity() != null && adapter != null) getActivity().runOnUiThread(() -> adapter.submit(combined));
                }
                @Override public void onError(Throwable t) { }
            }); } catch (Throwable t) { Log.w("RecipesFragment", "remote search error", t); }
        }
    }

    private boolean safeIsChecked(Chip chip) {
        try { return chip.isChecked(); } catch (Throwable t) { return false; }
    }

    private void showSafeError() {
        try {
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), getString(R.string.error_loading_recipes), android.widget.Toast.LENGTH_SHORT).show();
            }
        } catch (Throwable ignored) {}
    }

    private boolean chipChecked(int id) {
        try {
            if (rootView == null) return false;
            View v = rootView.findViewById(id);
            return (v instanceof Chip) && ((Chip) v).isChecked();
        } catch (Throwable t) { return false; }
    }
}





