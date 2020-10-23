package fr.asdl.minder.sharing.files

interface FileAccessor {

    fun createFile(
        fileName: String,
        fileType: String,
        content: ByteArray,
        callback: FileCreationCallBack
    )

    fun readFile(
        fileType: String,
        callback: FileOpeningCallBack
    )

}