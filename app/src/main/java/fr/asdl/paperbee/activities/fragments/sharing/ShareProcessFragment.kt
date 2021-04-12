package fr.asdl.paperbee.activities.fragments.sharing

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.asdl.paperbee.PaperBeeApplication
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.fragments.SharingFragment
import fr.asdl.paperbee.sharing.files.FileAccessor

class ShareProcessFragment : ShareBaseFragment(), FileAccessor {

    override val layoutId: Int = R.layout.share_process
    private var serviceIntent: Intent? = null

    override fun onLayoutInflated(view: View) {
        this.setToolBarIsClose(false)
        val orig = (this.parentFragment as? SharingFragment)
            ?: throw java.lang.IllegalStateException("Parent fragment required")
        if (orig.shareOptions.method == SharingMethod.FILE) {
            with(view.findViewById<View>(R.id.next)) {
                this.scaleX = 0f
                this.scaleY = 0f
                this.isEnabled = false
            }
            view.findViewById<View>(R.id.share_file_group).visibility = View.VISIBLE
            view.findViewById<View>(R.id.share_to_file_button).setOnClickListener {
                it.isEnabled = false
                orig.shareOptions.process(this, orig.selection, { success ->
                    if (success)
                        activity?.supportFragmentManager?.popBackStack(
                            orig.tag,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                    else
                        view.findViewById<TextView>(R.id.share_to_file_header).text =
                            getString(R.string.share_to_file_failed_retry)
                }) {  it.isEnabled = true }
            }
        } else if (orig.shareOptions.method == SharingMethod.NFC) {
            view.findViewById<View>(R.id.share_nfc_group).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.share_nfc_message).setText(R.string.share_nfc_current)
            orig.shareOptions.process(this, orig.selection, { })
            val fab = view.findViewById<FloatingActionButton>(R.id.next)
            fab.isEnabled = orig.shareOptions.isStoppable()
            fab.setOnClickListener {
                fab.apply {
                    this.scaleX = 0f
                    this.scaleY = 0f
                    this.isEnabled = false
                }
                view.findViewById<TextView>(R.id.share_nfc_message).text =
                    getString(R.string.share_nfc_stopped)
                orig.shareOptions.forceStop(this)
            }
        }
    }

    internal fun sendMessage(
        byteArray: ByteArray?,
        mimeType: String = "application/vnd.${requireContext().packageName}"
    ) {
        if (byteArray == null) {
            requireActivity().stopService(this.serviceIntent)
            this.serviceIntent = null
            (this.requireActivity().application as PaperBeeApplication).apply {
                nfcServiceListenerFrom = null
                nfcServiceListenerTo = null
            }
            return
        }
        val typeBytes = mimeType.toByteArray()
        val record1 = NdefRecord(NdefRecord.TNF_MIME_MEDIA, typeBytes, null, byteArray)
        val record2 = NdefRecord.createApplicationRecord(requireContext().packageName)
        val ndef = NdefMessage(arrayOf(record1, record2))
        (this.requireActivity().application as PaperBeeApplication).apply {
            nfcServiceListenerFrom = { ndef }
            nfcServiceListenerTo = {
                when (it) {
                    NfcShareService.RESULT_CODE_READING -> {
                        requireView().findViewById<TextView>(R.id.share_nfc_message).setText(R.string.share_nfc_current_running)
                        requireView().findViewById<View>(R.id.share_nfc_progress).visibility = View.VISIBLE
                    }
                    NfcShareService.RESULT_CODE_READ_FINISHED -> {
                        serviceIntent = null
                        activity?.supportFragmentManager?.popBackStack(
                            (this@ShareProcessFragment.parentFragment as? SharingFragment)!!.tag,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                    }
                    NfcShareService.RESULT_CODE_ERROR -> {
                        requireView().findViewById<TextView>(R.id.share_nfc_message).setText(R.string.share_nfc_current_error)
                    }
                }
            }
        }
        this.serviceIntent = Intent(requireActivity(), NfcShareService::class.java)
        requireActivity().startService(this.serviceIntent!!)
    }

    override fun onPause() {
        if (this.serviceIntent != null)
            requireActivity().stopService(this.serviceIntent!!)
        super.onPause()
    }

    override fun onResume() {
        if (this.serviceIntent != null)
            requireActivity().startService(this.serviceIntent!!)
        super.onResume()
    }

}