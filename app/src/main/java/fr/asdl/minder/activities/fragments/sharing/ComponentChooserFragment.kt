package fr.asdl.minder.activities.fragments.sharing

import android.view.View
import fr.asdl.minder.R

class ComponentChooserFragment : ShareBaseFragment() {

    override val layoutId: Int = R.layout.share_chooser

    override fun onLayoutInflated(view: View) {
        this.setToolBarIsClose(true)
        view.findViewById<View>(R.id.next).setOnClickListener {
            val orig = this@ComponentChooserFragment.getSharingFragment()
            orig?.displayFragment(OptionsFragment(), "optionShare${orig.getNotableId()}")
        }
    }

}