package fr.asdl.minder.sharing.files

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import fr.asdl.minder.R
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Access result values
 */
enum class FileAccessContext {
    CREATION, READ
}

enum class FileAccessResult(
    val success: Boolean,
    @StringRes private val actionDetailsCreation: Int,
    @StringRes private val actionDetailsOpening: Int
) {
    CANCELLED(false, 0, 0),
    ERROR(false, R.string.shared_to_file_exception, R.string.shared_from_file_exception),
    ACCESSED(true, R.string.shared_to_file_ok, R.string.shared_from_file_ok);

    fun hasPerformed() = this != CANCELLED

    @StringRes
    fun getActionDetails(accessContext: FileAccessContext): Int {
        return when (accessContext) {
            FileAccessContext.CREATION -> this.actionDetailsCreation
            FileAccessContext.READ -> this.actionDetailsOpening
        }
    }
}

/**
 * File Access Objects
 */
class FileAccess(val result: FileAccessResult, val uri: Uri?) {
    constructor(result: FileAccessResult) : this(result, null)
}
typealias FileAccessCallback = (Context, FileAccess) -> Unit
typealias FileCreationCallBack = (FileAccessResult) -> Unit
typealias FileOpeningCallBack = (FileAccessResult, ByteArray?) -> Unit

sealed class FutureFileAccess(val onAccessed: FileAccessCallback)
class FutureFileCreation(content: ByteArray, onCreated: FileCreationCallBack) :
    FutureFileAccess({ ctx, res ->
        if (res.result.success) {
            ctx.applicationContext.contentResolver.openFileDescriptor(res.uri!!, "w")?.use {
                FileOutputStream(it.fileDescriptor).write(content)
            }
        }
        onCreated.invoke(res.result)
    })

class FutureFileOpening(onOpened: FileOpeningCallBack) : FutureFileAccess({ ctx, res ->
    if (res.result.success) {
        ctx.applicationContext.contentResolver.openFileDescriptor(res.uri!!, "r")?.use {
            onOpened.invoke(res.result, FileInputStream(it.fileDescriptor).readBytes())
        }
    } else {
        onOpened.invoke(res.result, null)
    }
})