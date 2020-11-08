package fr.asdl.paperbee.view

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import androidx.appcompat.widget.Toolbar

/**
 * This toolbar fixes the WindowInsets that adds a bottom padding to the toolbar when the keyboard
 * is open.
 */
class ActionToolbar(context: Context, attributeSet: AttributeSet) : Toolbar(context, attributeSet),
    WindowInsetConsumer {

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        return this.useWindowInsets(insets, action = { inset ->
            this.setPadding(
                this.paddingLeft,
                inset.systemWindowInsetTop,
                this.paddingRight,
                this.paddingBottom
            )
        })
    }

}