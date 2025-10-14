package com.kephas.appcuisinereste;

import android.app.Application;
import timber.log.Timber;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialiser Timber pour les logs (optionnel mais utile)
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}