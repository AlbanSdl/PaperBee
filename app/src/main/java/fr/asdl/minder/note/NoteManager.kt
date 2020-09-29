package fr.asdl.minder.note

import android.content.Context
import fr.asdl.minder.IntAllocator
import fr.asdl.minder.R
import fr.asdl.minder.preferences.SavedDataDirectory
import fr.asdl.minder.view.sentient.DataHolder

class NoteManager(private val context: Context, idAllocator: IntAllocator) : NoteFolder(context.getString(R.string.app_name), idAllocator = idAllocator, noteManager = null, parentId = null) {

    companion object {
        const val ROOT_ID = -1
        const val TRASH_ID = -2
    }

    private val dataDirectory = SavedDataDirectory(this.context.getString(R.string.notes_directory_name), context)
    private val serializer = NoteSerializer()
    private val trash = NoteFolder(context.getString(R.string.trash_can), noteManager = this, idAllocator = idAllocator, parentId = null)

    init {
        this.noteManager = this
        this.trash.notify = true
    }

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
        // Put everything else in the trash
        trash.id = TRASH_ID
        saved.forEach { trash.add(it) }
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

    override fun findElementById(id: Int?): DataHolder? {
        if (id == TRASH_ID) return trash
        return super.findElementById(id)
    }

    override fun shouldNotify(): Boolean = true
    override var id: Int? = ROOT_ID
    override var order: Int = 0

}