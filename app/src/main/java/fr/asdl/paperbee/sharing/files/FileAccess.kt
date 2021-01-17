package fr.asdl.paperbee.sharing.files

import android.net.Uri
import androidx.annotation.StringRes
import fr.asdl.paperbee.R

/**
 * Used to retrieve the appropriate String of [FileAccessResult.getActionDetails]
 */
enum class FileAccessContext {
    CREATION, READ
}

/**
 * Represents the status of a file access request.
 */
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
 * Used when accessing a file.
 * Contains the uri the user selected (if applicable)
 */
internal class FileAccess(val result: FileAccessResult, val uri: Uri?) {
    constructor(result: FileAccessResult) : this(result, null)
}

/**
 * Represents an accessed file.
 * Contains a [FileAccessResult] which indicates whether the file was reached (modified/read)
 * with the file data if applicable (when reading the file and getting a [FileAccessResult.ACCESSED]
 * result)
 */
class AccessedFile(val result: FileAccessResult, val data: ByteArray?)