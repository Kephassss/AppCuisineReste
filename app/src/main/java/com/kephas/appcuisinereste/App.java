package com.kephas.appcuisinereste;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.repasdelaflemme.app.data.PrefPantryStore;
import com.repasdelaflemme.app.util.CrashLogger;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase if google-services.json is present (safe even if absent)
        try {
            FirebaseApp.initializeApp(this);
        } catch (Throwable ignored) { }

        // Global uncaught exception handler: log locally and forward to Crashlytics
        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                Log.e("App", "Uncaught exception in thread " + (t != null ? t.getName() : "unknown"), e);
                try { CrashLogger.write(getApplicationContext(), t, e); } catch (Throwable ignored) { }
                try { FirebaseCrashlytics.getInstance().recordException(e); } catch (Throwable ignored) { }
            } finally {
                if (previous != null) previous.uncaughtException(t, e);
            }
        });

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

