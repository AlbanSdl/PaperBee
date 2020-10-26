package fr.asdl.paperbee.activities.fragments.sharing

import com.google.android.material.snackbar.Snackbar
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.fragments.AppFragment
import fr.asdl.paperbee.note.Notable
import fr.asdl.paperbee.sharing.ShareProcess
import fr.asdl.paperbee.sharing.files.FileAccessContext

class ShareOptions {

    private val shareProcess = ShareProcess()
    private var sharingStarted = false
    var method: SharingMethod = SharingMethod.FILE

    // Options for SharingMethod.FILE
    var password: String = ""

    /**
     * Uses the current configuration to share asynchronously the given data.
     */
    fun process(context: AppFragment, data: List<Notable<*>>, callback: (Boolean) -> Unit) {
        when (this.method) {
            SharingMethod.NFC -> {
                TODO()
            }
            SharingMethod.FILE -> {
                val encryptedByteArray =
                    this.shareProcess.encrypt(if (this.password.isEmpty()) null else password, data)
                context.createFile(
                    context.getString(R.string.share_to_file_filename),
                    null,
                    encryptedByteArray
                ) {
                    sharingStarted = false
                    if (it.hasPerformed()) {
                        Snackbar.make(
                            context.activity!!.findViewById(R.id.main),
                            it.getActionDetails(FileAccessContext.CREATION),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        callback.invoke(it.success)
                    }
                }
            }
        }
        sharingStarted = true
    }

    /**
     * Stops synchronously the current operation
     */
    fun forceStop() {
        TODO()
    }

    /**
     * Retrieves whether the share option is stoppable (eg. you can't stop an export to a file
     * as it is too fast)
     */
    fun isStoppable(): Boolean {
        return this.method != SharingMethod.FILE && this.sharingStarted
    }

    /**
     * Retrieves whether the given data is correct and whether the sharing process can be
     * instantiated.
     */
    fun isCorrect(): Boolean {
        return true
    }

}