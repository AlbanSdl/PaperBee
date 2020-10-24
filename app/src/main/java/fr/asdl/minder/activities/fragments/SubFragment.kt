package fr.asdl.minder.activities.fragments

import android.app.Activity
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import fr.asdl.minder.R

interface SubFragment {

    fun getSharedViews(): List<View> = listOf()
    fun getActivity(): Activity?
    @IdRes
    fun getToolbarId(): Int
    @DrawableRes
    fun getDrawableRes(): Int = R.drawable.selector_close_back
    fun setToolBarIsClose(isClose: Boolean) {
        val toolbar = getActivity()?.findViewById<Toolbar>(getToolbarId()) ?: return
        if (toolbar.navigationIcon == null)
            toolbar.setNavigationIcon(getDrawableRes())
        // We try to get the navigation ImageView (the view actually) that stores the DrawableStates
        // and resets them depending on its own states
        try {
            val field = Toolbar::class.java.getDeclaredField("mNavButtonView")
            field.isAccessible = true
            val button = (field.get(toolbar) as ImageView)
            button.setImageState(IntArray(1) {
                when {
                    isClose -> -R.attr.state_changed
                    else -> R.attr.state_changed
                }
            }, true)
        } catch (e: Exception) {
            val state = toolbar.navigationIcon?.state
            toolbar.navigationIcon?.state = IntArray(state!!.size + 1) {
                when {
                    it < state.size -> state[it]
                    isClose -> -R.attr.state_changed
                    else -> R.attr.state_changed
                }
            }
        }
        toolbar.setNavigationOnClickListener { getActivity()?.onBackPressed() }
    }

}