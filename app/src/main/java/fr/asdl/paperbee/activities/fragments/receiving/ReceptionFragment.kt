package fr.asdl.paperbee.activities.fragments.receiving

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.view.View
import android.widget.TextView
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.fragments.ImportFragment
import fr.asdl.paperbee.nfc.NfcTag
import fr.asdl.paperbee.activities.fragments.sharing.SharingMethod
import fr.asdl.paperbee.exceptions.IncompatibleVersionException
import fr.asdl.paperbee.exceptions.WrongPasswordException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

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
                getScope().launch {
                    val result = orig.readFile(null)
                    if (result == null) {
                        this@ReceptionFragment.requireActivity().runOnUiThread {
                            view.findViewById<TextView>(R.id.share_from_file_header).text =
                                getString(R.string.share_from_file_failed_retry)
                        }
                    } else {
                        orig.shareData = result
                        try {
                            orig.content = orig.shareProcess.decryptFromFile(null, result)
                        } catch (e: WrongPasswordException) {
                        } catch (e: IncompatibleVersionException) {
                            this@ReceptionFragment.requireActivity().runOnUiThread {
                                view.findViewById<TextView>(R.id.share_from_file_header).text =
                                    getString(R.string.share_from_file_failed_compat)
                            }
                            return@launch
                        }
                        this@ReceptionFragment.requireActivity().runOnUiThread {
                            orig.displayFragment(ReceptionOptionsFragment(), "shareImportOptions")
                        }
                    }
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

    override fun onNdefMessage(nfcTag: NfcTag?) {
        if (nfcTag != null && nfcTag.readData().isNotEmpty()) {
            getScope().launch(Dispatchers.IO) {
                // Notify sender that data has been read
                val typeBytes = "application/vnd.${requireContext().packageName}".toByteArray()
                val record1 = NdefRecord(NdefRecord.TNF_MIME_MEDIA, typeBytes, null, byteArrayOf(0x00, 0x01))
                val record2 = NdefRecord.createApplicationRecord(requireContext().packageName)
                val message = NdefMessage(record1, record2)
                nfcTag.writeData(message)
                // Use the data we read from tag
                val data = nfcTag.readData()[0].records[0].payload
                val importFrag = this@ReceptionFragment.parentFragment as ImportFragment
                try {
                    importFrag.content = importFrag.shareProcess.decryptFromFile(null, data)
                    this@ReceptionFragment.requireActivity().runOnUiThread {
                        importFrag.displayFragment(ReceptionOptionsFragment(), "shareImportOptions")
                    }
                } catch (e: Exception) {
                    when (e) {
                        is IncompatibleVersionException, is WrongPasswordException -> this@ReceptionFragment.requireActivity().runOnUiThread {
                            requireView().findViewById<TextView>(R.id.share_nfc_message).text =
                                getString(R.string.share_from_nfc_incompatible_version)
                        }
                        else -> throw e
                    }
                }
            }
        } else {
            requireView().findViewById<TextView>(R.id.share_nfc_message).text =
                getString(R.string.share_from_nfc_incompatible)
        }
        super.onNdefMessage(nfcTag)
    }

}