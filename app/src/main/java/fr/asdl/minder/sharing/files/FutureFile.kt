package fr.asdl.minder.sharing.files

class FutureFile(val content: ByteArray, val onCreated: (FileCreationResult) -> Unit)