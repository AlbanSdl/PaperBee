package fr.asdl.minder.note

import android.content.Context
import android.os.Build
import fr.asdl.minder.IntAllocator
import fr.asdl.minder.R
import fr.asdl.minder.preferences.SavedDataDirectory
import fr.asdl.minder.view.sentient.DataHolderList
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class NoteManager(private val context: Context) : DataHolderList<Note>() {
    private val dataDirectory = SavedDataDirectory(this.context.getString(R.string.notes_directory_name), context)
    private val notes = LinkedList<Note>()
    private val idAllocator = IntAllocator(this.getNoteIds())
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

    private fun getNoteIds(): ArrayList<Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ArrayList(notes.stream().map { it.id!! }.collect(Collectors.toList()))
        } else {
            val r = ArrayList<Int>()
            for (note in notes) r.add(note.id!!)
            r
        }
    }
}