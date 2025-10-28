package com.repasdelaflemme.app.ui.detail;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.repasdelaflemme.app.R;
import com.repasdelaflemme.app.data.AssetsRepository;
import com.repasdelaflemme.app.data.model.Recipe;
import java.util.Locale;

public class CookingFragment extends Fragment {

    private static final String KEY_INDEX = "step_index";
    private Recipe recipe;
    private int index = 0;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean speaking = false;
    private ImageView cookingIcon;

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
        MaterialButton btnPlayPause = view.findViewById(R.id.btnPlayPause);
        cookingIcon = view.findViewById(R.id.cookingIcon);
        View swipeIndicator = view.findViewById(R.id.swipeIndicator);
        ImageView swipeIcon = view.findViewById(R.id.swipeIcon);

        if (recipe == null || recipe.steps == null || recipe.steps.isEmpty()) {
            title.setText(getString(R.string.app_name));
            header.setText("");
            current.setText(getString(R.string.error_loading_recipes));
            btnPrev.setEnabled(false);
            btnNext.setEnabled(false);
            btnPlayPause.setEnabled(false);
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
            updateCookingIconAnimation();
        };

        btnPrev.setOnClickListener(v -> {
            index = Math.max(0, index - 1);
            stopTts();
            render.run();
        });
        btnNext.setOnClickListener(v -> {
            if (index < recipe.steps.size() - 1) {
                index++;
                stopTts();
                render.run();
            } else {
                showSuccessThenExit();
            }
        });

        btnPlayPause.setOnClickListener(v -> {
            if (!ttsReady) return;
            if (speaking) {
                tts.stop();
                speaking = false;
                btnPlayPause.setText(R.string.step_play);
                try { btnPlayPause.setIconResource(R.drawable.ic_play); } catch (Throwable ignored) {}
            } else {
                tts.speak(recipe.steps.get(index), TextToSpeech.QUEUE_FLUSH, null, "step");
                speaking = true;
                btnPlayPause.setText(R.string.step_pause);
                try { btnPlayPause.setIconResource(R.drawable.ic_pause); } catch (Throwable ignored) {}
            }
            updateCookingIconAnimation();
            try { View st = getView().findViewById(R.id.txtCurrentStep); if (st != null) st.animate().alpha(0f).setDuration(60).withEndAction(() -> st.animate().alpha(1f).setDuration(160).start()).start(); } catch (Throwable ignored) {}
        });

        // Init TTS
        tts = new TextToSpeech(requireContext(), status -> {
            ttsReady = status == TextToSpeech.SUCCESS;
            if (ttsReady) {
                try { tts.setLanguage(Locale.getDefault()); } catch (Exception ignored) {}
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
        stopTts();
        stopCookingIconAnimation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_INDEX, index);
    }

    private void stopTts() {
        if (tts != null) tts.stop();
        speaking = false;
        View v = getView();
        if (v != null) {
            MaterialButton btn = v.findViewById(R.id.btnPlayPause);
            if (btn != null) { btn.setText(R.string.step_play); try { btn.setIconResource(R.drawable.ic_play); } catch (Throwable ignored) {} }
        }
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

    private void updateCookingIconAnimation() {
        if (cookingIcon == null) return;
        if (speaking) {
            // Keyframes bounce via code (CSS-like)
            com.repasdelaflemme.app.ui.common.AnimUtils.bounceKeyframes(cookingIcon, 8f, 2000L, true);
        } else {
            stopCookingIconAnimation();
        }
    }

    private void stopCookingIconAnimation() {
        if (cookingIcon != null) {
            cookingIcon.clearAnimation();
            // also clear any running ObjectAnimators by resetting translation
            cookingIcon.setTranslationY(0f);
        }
    }
}
