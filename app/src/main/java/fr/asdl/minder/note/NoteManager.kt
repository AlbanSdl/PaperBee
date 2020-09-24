package fr.asdl.minder.note

import android.content.Context
import fr.asdl.minder.IntAllocator
import fr.asdl.minder.R
import fr.asdl.minder.preferences.SavedDataDirectory
import fr.asdl.minder.view.sentient.DataHolderList
import java.util.*

class NoteManager(private val context: Context) : DataHolderList<Note>() {
    private val dataDirectory = SavedDataDirectory(this.context.getString(R.string.notes_directory_name), context)
    private val notes = LinkedList<Note>()
    private val idAllocator = IntAllocator()
    private val serializer = NoteSerializer()

    init {
        dataDirectory.loadData(this, this.serializer)
    }

    override fun retrieveContent(): LinkedList<Note> {
        return notes
    }

    override fun save(element: Note) {
        if (element.id == null) {
            element.id = idAllocator.allocate()
        }
        if (element.noteManager == null) {
            element.noteManager = this
            idAllocator.forceAllocate(element.id!!)
        }
        this.dataDirectory.saveDataAsync(element, serializer = this.serializer)
    }

    override fun delete(element: Note) {
        if (element.id == null) return
        this.idAllocator.release(element.id!!)
        this.dataDirectory.saveDataAsync(id = element.id!!, serializer = this.serializer)
    }
}