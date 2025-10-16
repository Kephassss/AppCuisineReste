package com.repasdelaflemme.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrefPantryStore {
    private static final String PREF = "pantry_store";
    private static final String KEY_ING_IDS = "ingredient_ids";

    private final SharedPreferences prefs;

    public PrefPantryStore(Context context) {
        this.prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public List<String> getIngredientIds() {
        String json = prefs.getString(KEY_ING_IDS, "[]");
        List<String> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i));
            }
        } catch (JSONException ignored) { }
        return list;
    }

    public void setIngredientIds(List<String> ids) {
        JSONArray arr = new JSONArray();
        for (String id : ids) arr.put(id);
        prefs.edit().putString(KEY_ING_IDS, arr.toString()).apply();
    }

    public void add(String id) {
        Set<String> set = new HashSet<>(getIngredientIds());
        if (id != null && !id.isEmpty()) {
            set.add(id);
            set(idArray(set));
        }
    }

    public void remove(String id) {
        Set<String> set = new HashSet<>(getIngredientIds());
        set.remove(id);
        set(idArray(set));
    }

    private void set(List<String> ids) {
        setIngredientIds(ids);
    }

    private static List<String> idArray(Set<String> set) {
        return new ArrayList<>(set);
    }
}

