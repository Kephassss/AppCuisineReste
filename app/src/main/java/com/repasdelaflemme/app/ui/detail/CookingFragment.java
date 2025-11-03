package com.repasdelaflemme.app.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.repasdelaflemme.app.R;
import com.repasdelaflemme.app.data.AssetsRepository;
import com.repasdelaflemme.app.data.model.Recipe;
 

public class CookingFragment extends Fragment {

    private static final String KEY_INDEX = "step_index";
    private Recipe recipe;
    private int index = 0;
    

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cooking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            index = savedInstanceState.getInt(KEY_INDEX, 0);
        }

        String recipeId = getArguments() != null ? getArguments().getString("recipeId") : null;
        recipe = recipeId != null ? AssetsRepository.findRecipeById(requireContext(), recipeId) : null;

        TextView title = view.findViewById(R.id.txtRecipeTitle);
        TextView header = view.findViewById(R.id.txtStepHeader);
        TextView current = view.findViewById(R.id.txtCurrentStep);
        ProgressBar progress = view.findViewById(R.id.progressSteps);
        MaterialButton btnPrev = view.findViewById(R.id.btnPrev);
        MaterialButton btnNext = view.findViewById(R.id.btnNext);
        View swipeIndicator = view.findViewById(R.id.swipeIndicator);
        ImageView swipeIcon = view.findViewById(R.id.swipeIcon);

        if (recipe == null || recipe.steps == null || recipe.steps.isEmpty()) {
            title.setText(getString(R.string.app_name));
            header.setText("");
            current.setText(getString(R.string.error_loading_recipes));
            btnPrev.setEnabled(false);
            btnNext.setEnabled(false);
            
            return;
        }

        title.setText(recipe.title);
        progress.setMax(100);

        Runnable render = () -> {
            int total = recipe.steps.size();
            int safeIndex = Math.max(0, Math.min(index, total - 1));
            index = safeIndex;
            header.setText(getString(R.string.step_progress, safeIndex + 1, total));
            current.setText(recipe.steps.get(safeIndex));
            progress.setProgress((int) (((safeIndex + 1) / (float) total) * 100));
            btnPrev.setEnabled(safeIndex > 0);
            btnNext.setText(safeIndex < total - 1 ? R.string.step_next : R.string.step_finish);
            
        };

        btnPrev.setOnClickListener(v -> {
            index = Math.max(0, index - 1);
            render.run();
        });
        btnNext.setOnClickListener(v -> {
            if (index < recipe.steps.size() - 1) {
                index++;
                render.run();
            } else {
                showSuccessThenExit();
            }
        });

        render.run();

        // Swipe hint animation (auto-hide after few seconds)
        if (swipeIcon != null) {
            swipeIcon.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.swipe_hint));
        }
        if (swipeIndicator != null) {
            swipeIndicator.setAlpha(0f);
            swipeIndicator.animate().alpha(1f).setDuration(200).start();
            swipeIndicator.postDelayed(() -> swipeIndicator.animate().alpha(0f).setDuration(300).withEndAction(() -> swipeIndicator.setVisibility(View.GONE)).start(), 4000);
        }

        // Simple swipe detection on the step content
        View scroll = view.findViewById(R.id.scrollSteps);
        if (scroll != null) {
            scroll.setOnTouchListener(new View.OnTouchListener() {
                float startX;
                @Override public boolean onTouch(View v1, android.view.MotionEvent ev) {
                    switch (ev.getActionMasked()) {
                        case android.view.MotionEvent.ACTION_DOWN:
                            startX = ev.getX();
                            break;
                        case android.view.MotionEvent.ACTION_UP:
                            float dx = ev.getX() - startX;
                            if (Math.abs(dx) > 120) {
                                if (dx < 0) {
                                    btnNext.performClick();
                                } else {
                                    btnPrev.performClick();
                                }
                                if (swipeIndicator != null) swipeIndicator.setVisibility(View.GONE);
                                return true;
                            }
                            break;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Keep screen on during cooking
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_INDEX, index);
    }

    private void showSuccessThenExit() {
        View v = getView();
        if (v == null) { requireActivity().onBackPressed(); return; }
        View overlay = v.findViewById(R.id.successOverlay);
        View icon = v.findViewById(R.id.successIcon);
        if (overlay != null) overlay.setVisibility(View.VISIBLE);
        if (icon != null) {
            icon.setScaleX(0.6f); icon.setScaleY(0.6f); icon.setAlpha(0f);
            icon.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(350).withEndAction(() -> {
                v.postDelayed(() -> requireActivity().onBackPressed(), 700);
            }).start();
        } else {
            v.postDelayed(() -> requireActivity().onBackPressed(), 700);
        }
    }

    
}
