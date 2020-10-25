package fr.asdl.paperbee.view.rounded

import android.view.MotionEvent
import android.view.View

/**
 * Fixes the hitbox of Rounded Views.
 * This interface will require the re-definition of onTouchEvent methods.
 */
interface RoundedView {

    /**
     * Method implemented by [View.getWidth] as this interface is designed to be used on
     * [View] inherited classes.
     */
    fun getWidth(): Int

    /**
     * Calls [View.onTouchEvent]. In your implementation, just call
     * super<View>.onTouchEvent(event)
     */
    fun onTouchEvent(event: MotionEvent?, byPass: Boolean): Boolean

    /**
     * Calls [RoundedView.onTouchEvent]. In your implementation, just call
     * super<RoundedView>.onTouchEvent(event)
     */
    fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return this.onTouchEvent(event, true)
        val rad = this.getWidth() / 2
        val unSignedEventX = rad - event.x
        val unSignedEventY = rad - event.y
        return if (event.actionMasked != MotionEvent.ACTION_UP &&
            event.actionMasked != MotionEvent.ACTION_CANCEL &&
            unSignedEventX * unSignedEventX + unSignedEventY * unSignedEventY > rad * rad)
            false else this.onTouchEvent(event, true)
    }

}