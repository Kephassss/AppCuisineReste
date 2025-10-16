package com.repasdelaflemme.app.ui.main;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.repasdelaflemme.app.R;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Gestion des insets système (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController);

            // Lier la BottomNavigationView avec le NavController
            BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Main content padding to avoid overlap with bottom nav (like .main-content under a fixed nav)
            View content = findViewById(R.id.nav_host_fragment);
            ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
                Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                int left = v.getPaddingLeft();
                int right = v.getPaddingRight();
                int top = v.getPaddingTop();
                int extra = Math.round(80f * v.getResources().getDisplayMetrics().density); // padding-bottom: 80px (≈80dp)
                int bottom = sys.bottom + bottomNav.getHeight() + extra;
                v.setPadding(left, top, right, bottom);
                return insets;
            });

            // Floating Action Button: primary action per screen
            FloatingActionButton fab = findViewById(R.id.fab_main);
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                if (id == R.id.homeFragment) {
                    fab.show();
                    fab.setImageResource(R.drawable.ic_recipe);
                    fab.setContentDescription(getString(R.string.action_find_recipes));
                    fab.setOnClickListener(v -> {
                        Bundle args = new Bundle();
                        args.putBoolean("focusPantry", true);
                        navController.navigate(R.id.recipesFragment, args);
                    });
                } else if (id == R.id.recipesFragment) {
                    fab.show();
                    fab.setImageResource(R.drawable.ic_kitchen);
                    fab.setContentDescription(getString(R.string.action_choose_ingredients));
                    fab.setOnClickListener(v -> navController.navigate(R.id.pantryFragment));
                } else if (id == R.id.pantryFragment) {
                    fab.show();
                    fab.setImageResource(R.drawable.ic_recipe);
                    fab.setContentDescription(getString(R.string.action_find_recipes));
                    fab.setOnClickListener(v -> {
                        Bundle args = new Bundle();
                        args.putBoolean("focusPantry", true);
                        navController.navigate(R.id.recipesFragment, args);
                    });
                } else {
                    fab.hide();
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            return navHostFragment.getNavController().navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}
