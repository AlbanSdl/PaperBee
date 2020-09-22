package fr.asdl.minder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

public class Fade {

    public static void shortCrossFade(final View fadeOut, View fadeIn) {

        int animDuration = fadeOut.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime);

        fadeIn.setAlpha(0f);
        fadeIn.setVisibility(View.VISIBLE);
        fadeIn.animate()
                .alpha(1f)
                .setDuration(animDuration)
                .setListener(null);

        fadeOut.animate()
                .alpha(0f)
                .setDuration(animDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        fadeOut.setVisibility(View.GONE);
                    }
                });
    }

    public static void shortFadeIn(View fadeIn) {
        fadeIn(fadeIn, fadeIn.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    /**
     * Note that short normal time is set by default to 400 millis
     * @param fadeIn the view to fade in
     * @param millis the time in milliseconds to fade in the view
     */
    public static void fadeIn(View fadeIn, int millis) {
        fadeIn.setAlpha(0f);
        fadeIn.setVisibility(View.VISIBLE);
        fadeIn.animate()
                .alpha(1f)
                .setDuration(millis);
    }

    public static void shortFadeOut(final View fadeOut) {
        fadeOut(fadeOut, fadeOut.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    public static void fadeOut(final View fadeOut, int millis) {
        fadeOut.animate()
                .alpha(0f)
                .setDuration(millis)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        fadeOut.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fadeOut.setVisibility(View.GONE);
                    }
                });
    }

}
