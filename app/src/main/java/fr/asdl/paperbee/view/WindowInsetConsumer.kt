package fr.asdl.paperbee.view

import android.view.View
import android.view.WindowInsets
import androidx.core.view.WindowInsetsCompat

interface WindowInsetConsumer {

    /**
     * Measures the element. Designed to use [View.onMeasure] if the implementation if used on a
     * View object.
     */
    fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int)

    /**
     * Uses the WindowInset. Provides a lambda that allows you to use a [WindowInsetsCompat].
     * You may also provide a consumption type if you don't handle the consumption in the lambda.
     * The method can also update the size of the current view (and will do it by default)
     */
    fun useWindowInsets(
        insets: WindowInsets,
        action: ((inset: WindowInsetsCompat) -> Unit)? = null,
        updateMeasure: Boolean = true,
        consumption: WindowInsetConsumption? = null
    ): WindowInsets {
        val insetCompat: WindowInsetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets)
        action?.invoke(insetCompat)
        if (updateMeasure)
            this.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY)
        when (consumption) {
            WindowInsetConsumption.SYSTEM_WINDOW -> insetCompat.consumeSystemWindowInsets()
            WindowInsetConsumption.STABLE -> insetCompat.consumeStableInsets()
            WindowInsetConsumption.DISPLAY_CUTOUT -> insetCompat.consumeDisplayCutout()
        }
        return insetCompat.toWindowInsets() ?: insets

    }

}