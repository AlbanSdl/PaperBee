package fr.asdl.minder.activities.fragments.sharing

import android.view.View
import fr.asdl.minder.R
import fr.asdl.minder.note.Note

class OptionsFragment : ShareBaseFragment() {

    override val layoutId: Int = R.layout.share_options

    override fun onLayoutInflated(view: View) {
        this.setToolBarIsClose(this.getSharingFragment()?.getOpenedFrom() is Note)
    }

}