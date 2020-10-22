package fr.asdl.minder.activities.fragments.sharing

import android.view.View
import fr.asdl.minder.R

class OptionsFragment : ShareBaseFragment() {

    override val layoutId: Int = R.layout.share_options

    override fun onLayoutInflated(view: View) {
        this.setToolBarIsClose(false)
    }

}