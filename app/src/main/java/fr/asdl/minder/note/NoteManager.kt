package fr.asdl.minder.note

import android.content.Context
import fr.asdl.minder.IntAllocator
import fr.asdl.minder.R
import fr.asdl.minder.preferences.SavedDataDirectory
import fr.asdl.minder.view.sentient.DataHolderList
import java.util.*

class NoteManager(private val context: Context, idAllocator: IntAllocator) : DataHolderList<Note>(idAllocator, null, null) {

    private val dataDirectory = SavedDataDirectory(this.context.getString(R.string.notes_directory_name), context)
    override val contents = LinkedList<Note>()
    val serializer = NoteSerializer()

    fun load() {
        dataDirectory.loadData(this, this.serializer)
    }

    override fun save(element: Note) {
        element.noteManager = this
        if (element.noteManager == null) element.noteManager = this
        this.dataDirectory.saveDataAsync(element, serializer = this.serializer)
    }

    override fun delete(element: Note, oldId: Int) {
        this.dataDirectory.saveDataAsync(id = oldId, serializer = this.serializer)
    }

    override fun shouldNotify(): Boolean = true
    override var id: Int? = -1
    override var order: Int = 0

}