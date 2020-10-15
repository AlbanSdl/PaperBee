package fr.asdl.minder.view.transition

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.transition.Transition
import androidx.transition.TransitionValues

/**
 * This transition fades Background tint
 */
class BackgroundTint(context: Context, attributeSet: AttributeSet) : Transition(context, attributeSet) {

    private val propName = "fr.asdl.minder.view.transition:BackgroundTint:background"

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    private fun captureValues(transitionValues: TransitionValues) {
        transitionValues.values[propName] = transitionValues.view.backgroundTintList?.defaultColor
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues == null && endValues == null)
            return null
        val view = endValues?.view ?: startValues!!.view
        val startBackground = startValues?.values?.get(propName) ?: 0x00ffffff
        val endBackground = endValues?.values?.get(propName) ?: 0x00ffffff
        val argbEvaluator = ArgbEvaluator()
        if (startBackground is Int && endBackground is Int) {
            if (startBackground != endBackground) {
                val animator = ValueAnimator.ofFloat(0f, 1f)
                animator.addUpdateListener {
                    view.backgroundTintList = ColorStateList.valueOf(argbEvaluator.evaluate(it.animatedFraction, startBackground, endBackground) as Int)
                }
                return animator
            }
        }
        return null
    }

}