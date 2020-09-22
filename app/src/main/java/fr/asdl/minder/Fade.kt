package fr.asdl.minder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

object Fade {

    fun fadeIn(fadeIn: View) {
        fadeIn(fadeIn, fadeIn.context.resources.getInteger(android.R.integer.config_mediumAnimTime))
    }

    fun fadeIn(fadeIn: View, millis: Int) {
        if (fadeIn.animation?.hasEnded() == false) fadeIn.animation.cancel()
        fadeIn.alpha = 0f
        fadeIn.visibility = View.VISIBLE
        fadeIn.animate().alpha(1f).duration = millis.toLong()
    }

    fun fadeOut(fadeOut: View) {
        fadeOut(fadeOut, fadeOut.context.resources.getInteger(android.R.integer.config_mediumAnimTime))
    }

    fun fadeOut(fadeOut: View, millis: Int) {
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