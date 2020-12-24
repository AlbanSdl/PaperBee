package fr.asdl.paperbee.activities.fragments

import android.view.View
import androidx.appcompat.widget.Toolbar
import fr.asdl.paperbee.R

class PreferenceFragmentRoot : AppFragment() {

    override val layoutId: Int = R.layout.settings

    override fun onLayoutInflated(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.folder_toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        this.requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, PreferenceContentsFragment())
            .commit()
    }

    override fun shouldLockDrawer(): Boolean = true

}