package fr.asdl.paperbee.sharing.files

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
     * @param fileType the MIME type of the file. Only android registered types will be persistent.
     * Take a look at [this page](https://android.googlesource.com/platform/external/mime-support/+/master/mime.types)
     * for a list of types compatible across the android ecosystem. If you want to use a custom
     * data model pass "null" which will be an alias to "application/octet-stream".
     * @param content the [ByteArray] containing the bytes of your file. Use the same kind of values
     * as if you were using OutputStream#write.
     * @param callback the lambda to execute when the operation has ended. You can check it has been
     * performed using the [FileAccessResult.success] property. If this property is false, you can
     * check if the operation has been cancelled by the user or if it is an IOException (in that
     * case it's most likely an application error that should be fixed).
     */
    fun createFile(
        fileName: String,
        fileType: String?,
        content: ByteArray,
        callback: FileCreationCallBack
    )

    /**
     * Gives the user the opportunity to load the data of a file of his choice in the app.
     * The user will choose the precise file [android.net.Uri] with the DocumentsUI user interface.
     *
     * @param fileType the MIME type of the file. Take a look at
     * [this page](https://android.googlesource.com/platform/external/mime-support/+/master/mime.types)
     * for a list of types compatible across the android ecosystem. If you want to use a custom
     * data model pass "null" which will be an alias to "application/octet-stream".
     * @param callback the lambda to execute when the operation has ended. You must first check that
     * [FileAccessResult.success] is true before accessing the [ByteArray] which contains the data
     * of the file. If the property is false, you can check if the operation has been cancelled
     * by the user or if it is an IOException (in that case it's most likely an application error
     * that should be fixed).
     */
    fun readFile(
        fileType: String?,
        callback: FileOpeningCallBack
    )

}