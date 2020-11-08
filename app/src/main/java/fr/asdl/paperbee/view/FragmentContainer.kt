package fr.asdl.paperbee.view

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.core.view.children

/**
 * Fixes fitsSystemWindows=true property for fragments and fragment transitions.
 * With a [FragmentContainer] the fragments will all receive the WindowInsets, including the
 * vanishing fragment and the incoming one.
 * The fragments are NOT re-measured to apply changes (re-measure children when they are modified).
 */
class FragmentContainer(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet), WindowInsetConsumer {

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        var consumed = false
        children.forEach { child ->
            val childResult = child.dispatchApplyWindowInsets(insets)
            if (childResult.isConsumed)
                consumed = true
        }
        return this.useWindowInsets(
            insets,
            consumption = if (consumed) WindowInsetConsumption.SYSTEM_WINDOW else null,
            updateMeasure = false
        )
    }

}