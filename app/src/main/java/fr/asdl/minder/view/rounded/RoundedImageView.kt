package fr.asdl.minder.view.rounded

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView


/**
 * An ImageView with a circle as a hitbox
 */
open class RoundedImageView(context: Context, attributeSet: AttributeSet) : AppCompatImageView(context, attributeSet), RoundedView {
    override fun onTouchEvent(event: MotionEvent?, byPass: Boolean): Boolean = super<AppCompatImageView>.onTouchEvent(event)
    override fun onTouchEvent(event: MotionEvent?): Boolean = super<RoundedView>.onTouchEvent(event)
}