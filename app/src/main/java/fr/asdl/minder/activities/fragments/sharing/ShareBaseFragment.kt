package fr.asdl.minder.activities.fragments.sharing

import androidx.appcompat.widget.Toolbar
import fr.asdl.minder.R
import fr.asdl.minder.activities.fragments.AppFragment
import fr.asdl.minder.activities.fragments.SharingFragment

abstract class ShareBaseFragment : AppFragment() {

    protected fun getSharingFragment(): SharingFragment? {
        return this.activity?.supportFragmentManager?.findFragmentById(R.id.folder_contents) as? SharingFragment
    }

    protected fun setToolBarIsClose(isClose: Boolean) {
        val toolbar = activity?.findViewById<Toolbar>(R.id.share_toolbar) ?: return
        if (toolbar.navigationIcon == null)
            toolbar.setNavigationIcon(R.drawable.selector_close_back)
        toolbar.navigationIcon?.state =
            IntArray(1) { if (isClose) -android.R.attr.state_activated else android.R.attr.state_activated }
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    }

}