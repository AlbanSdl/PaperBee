package fr.asdl.minder.activities.fragments.sharing

import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import fr.asdl.minder.R
import fr.asdl.minder.activities.fragments.AppFragment
import fr.asdl.minder.activities.fragments.SharingFragment

abstract class ShareBaseFragment : AppFragment() {

    override val shouldRetainInstance: Boolean = true

    protected fun getSharingFragment(): SharingFragment? {
        return this.activity?.supportFragmentManager?.findFragmentById(R.id.folder_contents) as? SharingFragment
    }

    protected fun setToolBarIsClose(isClose: Boolean) {
        val toolbar = activity?.findViewById<Toolbar>(R.id.share_toolbar) ?: return
        if (toolbar.navigationIcon == null)
            toolbar.setNavigationIcon(R.drawable.selector_close_back)
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
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    }

    open fun getSharedViews(): List<View> = listOf()

}