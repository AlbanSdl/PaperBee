package fr.asdl.minder.sharing.files

interface FileCreator {

    fun createFile(
        fileName: String,
        fileType: String,
        content: ByteArray,
        callback: (FileCreationResult) -> Unit
    )

}