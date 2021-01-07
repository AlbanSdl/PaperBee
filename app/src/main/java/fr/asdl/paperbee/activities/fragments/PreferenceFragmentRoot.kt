package fr.asdl.paperbee.activities.fragments

import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import fr.asdl.paperbee.R

class PreferenceFragmentRoot : AppFragment() {

    override val layoutId: Int = R.layout.settings

    override fun onLayoutInflated(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.folder_toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        this.childFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, PreferenceContentsFragment())
            .commit()
    }

    fun setToolbarTitle(@StringRes id: Int) {
        val toolbar = view?.findViewById<Toolbar>(R.id.folder_toolbar)
        toolbar?.setTitle(id)
    }

    override fun shouldLockDrawer(): Boolean = true

}