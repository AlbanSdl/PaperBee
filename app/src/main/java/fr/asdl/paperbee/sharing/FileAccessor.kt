package fr.asdl.paperbee.sharing

/**
 * Indicates that this Object can create and read files in the phone shared storage.
 * At the moment, this storage permissions are not persistent. That's why there is no save/write
 * file method (in that case we should store the Uri in the app preferences to retrieve the allowed
 * Uris - even after a device reboot).
 */
interface FileAccessor {

    /**
     * Gives the user the opportunity to save the data passed through [content] in a file saved in
     * shared storage (such as downloads or documents folders).
     *
     * @param fileName the default name of the file to save. The user will be able to modify this
     * value since he will choose the precise save [android.net.Uri].
     * @param content the [ByteArray] containing the bytes of your file. Use the same kind of values
     * as if you were using OutputStream#write.
     */
    suspend fun createFile(
        fileName: String,
        content: ByteArray
    ): Boolean

    /**
     * Gives the user the opportunity to load the data of a file of his choice in the app.
     * The user will choose the precise file [android.net.Uri] with the DocumentsUI user interface.
     */
    suspend fun readFile(fileType: String?): ByteArray?
}