package fr.asdl.minder.activities.fragments.sharing

import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import fr.asdl.minder.R
import fr.asdl.minder.sharing.files.FileCreator

class ShareProcessFragment : ShareBaseFragment(), FileCreator {

    override val layoutId: Int = R.layout.share_process

    override fun onLayoutInflated(view: View) {
        this.setToolBarIsClose(false)
        val orig = getSharingFragment() ?: return
        if (orig.shareOptions.method == SharingMethod.FILE) {
            with(view.findViewById<View>(R.id.next)) {
                this.scaleX = 0f
                this.scaleY = 0f
                this.isEnabled = false
            }
            view.findViewById<View>(R.id.share_file_group).visibility = View.VISIBLE
            view.findViewById<View>(R.id.share_to_file_button).setOnClickListener {
                orig.shareOptions.process(this, orig.selection) { success ->
                    if (success)
                        activity?.supportFragmentManager?.popBackStack(
                            orig.tag,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                    else
                        view.findViewById<TextView>(R.id.share_to_file_header).text =
                            getString(R.string.share_to_file_failed_retry)
                }
            }
        }
    }
}