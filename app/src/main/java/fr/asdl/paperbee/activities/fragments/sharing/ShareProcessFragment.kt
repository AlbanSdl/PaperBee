package fr.asdl.paperbee.activities.fragments.sharing

import android.nfc.*
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.fragments.SharingFragment
import fr.asdl.paperbee.sharing.files.FileAccessor
import kotlinx.coroutines.*

class ShareProcessFragment : ShareBaseFragment(), FileAccessor {

    override val layoutId: Int = R.layout.share_process
    private var ndefMessage: NdefMessage? = null

    override fun onLayoutInflated(view: View) {
        this.setToolBarIsClose(false)
        val orig = (this.parentFragment as? SharingFragment) ?: throw java.lang.IllegalStateException("Parent fragment required")
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
            orig.shareOptions.process(this, orig.selection) { }
            val fab = view.findViewById<FloatingActionButton>(R.id.next)
            fab.isEnabled = orig.shareOptions.isStoppable()
            fab.setOnClickListener {
                fab.apply {
                    this.scaleX = 0f
                    this.scaleY = 0f
                    this.isEnabled = false
                }
                view.findViewById<TextView>(R.id.share_nfc_message).text = getString(R.string.share_nfc_stopped)
                orig.shareOptions.forceStop(this)
            }
        }
    }

    internal fun sendMessage(byteArray: ByteArray?, mimeType: String = "application/vnd.${requireContext().packageName}") {
        if (byteArray == null) {
            this.ndefMessage = null
            return
        }
        val typeBytes = mimeType.toByteArray()
        val record1 = NdefRecord(NdefRecord.TNF_MIME_MEDIA, typeBytes, null, byteArray)
        val record2 = NdefRecord.createApplicationRecord(requireContext().packageName)
        this.ndefMessage = NdefMessage(arrayOf(record1, record2))
    }

    override fun onNdefMessage(nfcTag: NfcTag?) {
        if (this.ndefMessage != null && nfcTag != null) {
            GlobalScope.launch(Dispatchers.IO) {
                nfcTag.writeData(ndefMessage!!)
                ndefMessage = null
            }
        }
        super.onNdefMessage(nfcTag)
    }

}