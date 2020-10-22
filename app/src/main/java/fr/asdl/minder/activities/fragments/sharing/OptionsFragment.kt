package fr.asdl.minder.activities.fragments.sharing

import android.view.View
import fr.asdl.minder.R
import fr.asdl.minder.note.Note

class OptionsFragment : ShareBaseFragment() {

    override val layoutId: Int = R.layout.share_options

    override fun onLayoutInflated(view: View) {
        this.setToolBarIsClose(this.getSharingFragment()?.getOpenedFrom() is Note)
    }

    override fun getSharedViews(): List<View> {
        return listOf(this.view!!.findViewById(R.id.next))
    }

}