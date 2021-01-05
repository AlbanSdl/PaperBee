package fr.asdl.paperbee.activities.fragments.receiving

import android.view.View
import android.widget.TextView
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.fragments.ImportFragment
import fr.asdl.paperbee.activities.fragments.sharing.SharingMethod
import fr.asdl.paperbee.exceptions.WrongPasswordException

class ReceptionFragment : ReceptionBaseFragment() {

    override val layoutId: Int = R.layout.share_import_process

    override fun onLayoutInflated(view: View) {
        val orig = this.parentFragment as ImportFragment
        this.setToolBarIsClose(true)

        if (orig.method == SharingMethod.FILE) {
            with(view.findViewById<View>(R.id.next)) {
                this.scaleX = 0f
                this.scaleY = 0f
                this.isEnabled = false
            }
            view.findViewById<View>(R.id.share_file_group).visibility = View.VISIBLE
            view.findViewById<View>(R.id.share_from_file_button).setOnClickListener {
                orig.readFile(null) { res, data ->
                    if (res.success) {
                        orig.shareData = data
                        try {
                            orig.content = orig.shareProcess.decryptFromFile(null, data!!)
                        } catch (e: WrongPasswordException) {
                        }
                        orig.displayFragment(ReceptionOptionsFragment(), "shareImportOptions")
                    } else
                        view.findViewById<TextView>(R.id.share_from_file_header).text =
                            getString(R.string.share_from_file_failed_retry)
                }
            }
        } else if (orig.method == SharingMethod.NFC) {
            view.findViewById<View>(R.id.share_nfc_group).visibility = View.VISIBLE
        }
    }

    override fun getSharedViews(): List<View> {
        if (this.view == null) return super.getSharedViews()
        return listOf(this.requireView().findViewById(R.id.next))
    }

}