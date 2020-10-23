package fr.asdl.minder.activities.fragments.sharing

import com.google.android.material.snackbar.Snackbar
import fr.asdl.minder.R
import fr.asdl.minder.activities.fragments.AppFragment
import fr.asdl.minder.note.Notable
import fr.asdl.minder.sharing.ShareProcess
import fr.asdl.minder.sharing.files.FileCreationResult

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
                val encryptedByteArray = this.shareProcess.encrypt(if (this.password.isEmpty()) null else password, data)
                context.createFile(context.getString(R.string.share_to_file_filename), "application/mind", encryptedByteArray) {
                    if (it.hasTried) {
                        sharingStarted = false
                        Snackbar.make(
                            context.activity!!.findViewById(R.id.main),
                            it.str,
                            Snackbar.LENGTH_SHORT
                        ).show()
                        callback.invoke(it == FileCreationResult.CREATED)
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