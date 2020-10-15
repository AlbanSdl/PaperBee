package fr.asdl.minder.view.transition

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.transition.Transition
import androidx.transition.TransitionValues
import fr.asdl.minder.R

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

    private fun getTintColor(transitionValues: TransitionValues?, context: Context): Int {
        return transitionValues?.values?.get(propName) as Int? ?:
            (transitionValues?.view?.background as? ColorDrawable)?.color ?:
            ResourcesCompat.getColor(context.resources, R.color.dark, context.theme)
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues == null && endValues == null)
            return null
        val view = endValues?.view ?: startValues!!.view
        val startBackground = getTintColor(startValues, view.context)
        val endBackground = getTintColor(endValues, view.context)
        val argbEvaluator = ArgbEvaluator()
        if (startBackground != endBackground) {
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.addUpdateListener {
                view.backgroundTintList = ColorStateList.valueOf(argbEvaluator.evaluate(it.animatedFraction, startBackground, endBackground) as Int)
            }
            return animator
        }
        return null
    }

}