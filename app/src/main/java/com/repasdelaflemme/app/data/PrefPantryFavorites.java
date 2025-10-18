package com.repasdelaflemme.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrefPantryFavorites {
    private static final String PREF = "pantry_favorites";
    private static final String KEY = "fav_ids";
    private final SharedPreferences prefs;

    public PrefPantryFavorites(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public Set<String> get() {
        String json = prefs.getString(KEY, "[]");
        Set<String> out = new HashSet<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) out.add(arr.getString(i));
        } catch (Throwable ignored) {}
        return out;
    }

    public void set(Set<String> ids) {
        JSONArray arr = new JSONArray();
        if (ids != null) for (String id : ids) arr.put(id);
        prefs.edit().putString(KEY, arr.toString()).apply();
    }

    public void add(String id) { Set<String> s = get(); if (id != null) { s.add(id); set(s);} }
    public void remove(String id) { Set<String> s = get(); if (id != null) { s.remove(id); set(s);} }
    public boolean contains(String id) { return get().contains(id); }
}

