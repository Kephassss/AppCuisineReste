package com.kephas.appcuisinereste;

import android.app.Application;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import com.repasdelaflemme.app.data.PrefPantryStore;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Application initialisée
        // Ensure default pre-selections (tomate/oignon) are removed once.
        SharedPreferences appPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (!appPrefs.getBoolean("defaults_cleared", false)) {
            try {
                PrefPantryStore store = new PrefPantryStore(getApplicationContext());
                List<String> ids = new ArrayList<>(store.getIngredientIds());
                ids.remove("tomate");
                ids.remove("oignon");
                store.setIngredientIds(ids);
            } catch (Exception ignored) { }
            appPrefs.edit().putBoolean("defaults_cleared", true).apply();
        }
    }
}

