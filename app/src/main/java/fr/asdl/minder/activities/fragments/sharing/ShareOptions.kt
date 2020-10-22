package fr.asdl.minder.activities.fragments.sharing

import android.content.Context
import fr.asdl.minder.note.Notable
import fr.asdl.minder.sharing.ShareProcess

class ShareOptions {

    private val shareProcess = ShareProcess()
    private var sharingStarted = false
    var method: SharingMethod = SharingMethod.FILE

    // Options for SharingMethod.FILE
    var password: String = ""
    var fileName: String = ""

    /**
     * Uses the current configuration to share asynchronously the given data.
     */
    fun process(context: Context, data: List<Notable<*>>) {
        when (this.method) {
            SharingMethod.NFC -> {
                TODO()
            }
            SharingMethod.FILE -> {
                val encryptedByteArray = this.shareProcess.encrypt(if (this.password.isEmpty()) null else password, data)
                TODO()
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
        return this.method == SharingMethod.NFC || this.fileName.isNotEmpty()
    }

}