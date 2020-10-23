package fr.asdl.minder.activities.fragments.sharing

import android.view.View
import androidx.fragment.app.FragmentManager
import fr.asdl.minder.R
import fr.asdl.minder.sharing.files.FileCreator

class ShareProcessFragment : ShareBaseFragment(), FileCreator {

    override val layoutId: Int = R.layout.share_process

    override fun onLayoutInflated(view: View) {
        this.setToolBarIsClose(false)
        val orig = getSharingFragment()
        orig?.shareOptions?.process(this, orig.selection) {
            activity?.supportFragmentManager?.popBackStack(orig.tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

}