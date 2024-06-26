package fr.asdl.paperbee.activities.fragments.sharing

import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.fragments.SharingFragment
import fr.asdl.paperbee.sharing.files.FileAccessor

class ShareProcessFragment : ShareBaseFragment(), FileAccessor {

    override val layoutId: Int = R.layout.share_process

    override fun onLayoutInflated(view: View) {
        this.setToolBarIsClose(false)
        val orig = (this.parentFragment as? SharingFragment) ?: return
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
        } else if (orig.shareOptions.method == SharingMethod.NFC) {
            view.findViewById<View>(R.id.share_nfc_group).visibility = View.VISIBLE
        }
    }
}