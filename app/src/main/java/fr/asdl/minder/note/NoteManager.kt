package fr.asdl.minder.note

import android.content.Context
import fr.asdl.minder.IntAllocator
import fr.asdl.minder.R
import fr.asdl.minder.preferences.SavedDataDirectory

class NoteManager(private val context: Context, idAllocator: IntAllocator) : NoteFolder("", idAllocator = idAllocator, noteManager = null, parentId = null) {

    private val dataDirectory = SavedDataDirectory(this.context.getString(R.string.notes_directory_name), context)
    val serializer = NoteSerializer()

    fun load() {
        val saved = dataDirectory.loadData(this.serializer)
        fun fillFolder(folder: Notable<*>) {
            if (folder !is NoteFolder) return
            val stored = saved.filter { it.parentId == folder.id }
            saved.removeAll(stored)
            stored.sortedBy { it.order }.forEach {
                folder.add(it)
                fillFolder(it)
            }
        }
        fillFolder(this)
    }

    public override fun save(element: Notable<*>): Boolean {
        element.noteManager = this
        if (element.noteManager == null) element.noteManager = this
        this.dataDirectory.saveDataAsync(element, serializer = this.serializer)
        return true
    }

    public override fun delete(element: Notable<*>, oldId: Int): Boolean {
        this.dataDirectory.saveDataAsync(id = oldId, serializer = this.serializer)
        return true
    }

    override fun shouldNotify(): Boolean = true
    override var id: Int? = -1
    override var order: Int = 0

}