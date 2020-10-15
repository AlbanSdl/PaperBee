package fr.asdl.minder.view.rounded

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import fr.asdl.minder.R


/**
 * An ImageView with a circle as a hitbox with a ripple background effect.
 * IMPORTANT: the background cannot be set in XML or it will be reset !
 */
open class RoundedImageView(context: Context, attributeSet: AttributeSet) : AppCompatImageView(context, attributeSet), RoundedView {

    /**
     * Override it and return false if the background should not be set to a selectable
     * transparent background.
     */
    open fun doesOverrideBackground(): Boolean = true

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val array = context.obtainStyledAttributes(attributeSet, R.styleable.RoundedImageView)
            this.tooltipText = array.getString(R.styleable.RoundedImageView_tooltip)
            array.recycle()
        }
        if (this.doesOverrideBackground())
            this.background = ResourcesCompat.getDrawable(context.resources, R.drawable.selectable_circle, context.theme)
    }

    override fun onTouchEvent(event: MotionEvent?, byPass: Boolean): Boolean = super<AppCompatImageView>.onTouchEvent(event)
    override fun onTouchEvent(event: MotionEvent?): Boolean = super<RoundedView>.onTouchEvent(event)
}