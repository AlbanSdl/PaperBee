package fr.asdl.paperbee.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.core.view.children

/**
 * Fixes fitsSystemWindows=true property for fragments and fragment transitions.
 * With a [FragmentContainer] the fragments will all receive the WindowInsets, including the
 * vanishing fragment and the incoming one.
 * The fragments are re-measured to apply changes.
 */
class FragmentContainer(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    override fun onApplyWindowInsets(insets: WindowInsets?): WindowInsets {
        Log.e(javaClass.simpleName, "Window insets received !")
        if (insets == null) return super.onApplyWindowInsets(insets)
        var consumed = false
        children.forEach { child ->
            val childResult = child.dispatchApplyWindowInsets(insets)
            if (childResult.isConsumed)
                consumed = true
        }
        this.measure(MeasureSpec.EXACTLY, MeasureSpec.EXACTLY)
        return if (consumed) {
            @Suppress("DEPRECATION") // android is 100% stupid: the deprecation doesn't match older android versions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) WindowInsets.CONSUMED
            else insets.consumeSystemWindowInsets()
        } else insets
    }

}