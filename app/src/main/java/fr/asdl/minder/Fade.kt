package fr.asdl.minder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

object Fade {

    fun fadeIn(fadeIn: View, millis: Int = fadeIn.context.resources.getInteger(android.R.integer.config_mediumAnimTime)) {
        if (fadeIn.animation?.hasEnded() == false) fadeIn.animation.cancel()
        fadeIn.alpha = 0f
        fadeIn.visibility = View.VISIBLE
        fadeIn.animate().alpha(1f).setDuration(millis.toLong()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                fadeIn.alpha = 1f
            }
        })
    }

    fun fadeOut(fadeOut: View, millis: Int = fadeOut.context.resources.getInteger(android.R.integer.config_mediumAnimTime)) {
        if (fadeOut.animation?.hasEnded() == false) fadeOut.animation.cancel()
        fadeOut.animate().alpha(0f).setDuration(millis.toLong()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                fadeOut.visibility = View.GONE
            }
            override fun onAnimationEnd(animation: Animator) {
                fadeOut.visibility = View.GONE
            }
        })
    }
}