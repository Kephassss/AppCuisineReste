package com.repasdelaflemme.app;

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

        // Initialize Firebase safely (works even if google-services.json is absent)
        try { FirebaseApp.initializeApp(this); } catch (Throwable ignored) {}

        // Global uncaught exception handler: local log + Crashlytics
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                Log.e("App", "Uncaught exception in thread " + (t != null ? t.getName() : "unknown"), e);
                try { CrashLogger.write(getApplicationContext(), t, e); } catch (Throwable ignored) { }
                try { FirebaseCrashlytics.getInstance().recordException(e); } catch (Throwable ignored) { }
            } finally {
                if (previous != null) previous.uncaughtException(t, e);
            }
        });

        // One-time cleanup for default pre-selections
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

