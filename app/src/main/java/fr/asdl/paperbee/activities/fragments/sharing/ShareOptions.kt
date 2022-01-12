package fr.asdl.paperbee.activities.fragments.sharing

import com.google.android.material.snackbar.Snackbar
import fr.asdl.paperbee.R
import fr.asdl.paperbee.note.Notable
import fr.asdl.paperbee.note.Note
import fr.asdl.paperbee.sharing.ShareProcess
import fr.asdl.paperbee.view.sentient.DataHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShareOptions {

    private val shareProcess = ShareProcess()
    private var sharingStarted = false
    var method: SharingMethod = SharingMethod.FILE

    // Options for SharingMethod.FILE
    var password: String = ""

    /**
     * Uses the current configuration to share asynchronously the given data.
     */
    fun process(context: ShareProcessFragment, data: List<Notable<*>>, callback: (Boolean) -> Unit,
                processingFinished: (() -> Unit)? = null) {
        sharingStarted = true
        context.getScope().launch(Dispatchers.IO) {
            val encryptedByteArray =
                shareProcess.encryptToFile(if (password.isEmpty()) null else password, data) { it ->
                    val fullList = arrayListOf<DataHolder>()
                    fullList.addAll(data)
                    it.forEach { if (it is Note) fullList.addAll(it.db!!.findNoteContent(it.id)) }
                    return@encryptToFile fullList
                }
            if (processingFinished != null)
                context.requireActivity().runOnUiThread(processingFinished)
            when (this@ShareOptions.method) {
                SharingMethod.NFC -> {
                    context.requireActivity().runOnUiThread {
                        context.sendMessage(encryptedByteArray)
                    }
                }
                SharingMethod.FILE -> {
                    val success = context.createFile(context.getString(R.string.share_to_file_filename), encryptedByteArray)
                    Snackbar.make(
                        context.requireActivity().findViewById(R.id.main),
                        if (success) R.string.shared_to_file_ok else R.string.shared_to_file_exception,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    sharingStarted = false
                    callback.invoke(success)
                }
            }
        }
    }

    /**
     * Stops synchronously the current operation
     */
    fun forceStop(frag: ShareProcessFragment) {
        if (this.method == SharingMethod.NFC && this.sharingStarted) {
            frag.sendMessage(null)
            this.sharingStarted = false
        }
    }

    /**
     * Retrieves whether the share option is stoppable (eg. you can't stop an export to a file
     * as it is too fast)
     */
    fun isStoppable(): Boolean {
        return this.method == SharingMethod.NFC && this.sharingStarted
    }

    /**
     * Retrieves whether the given data is correct and whether the sharing process can be
     * instantiated.
     */
    fun isCorrect(): Boolean {
        return true
    }

}