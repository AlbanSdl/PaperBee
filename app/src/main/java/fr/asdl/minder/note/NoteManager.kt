package fr.asdl.minder.note

import android.content.Context
import android.os.Build
import fr.asdl.minder.IntAllocator
import fr.asdl.minder.R
import fr.asdl.minder.preferences.SavedDataDirectory
import fr.asdl.minder.view.DataHolder
import fr.asdl.minder.view.DataHolderList
import kotlinx.serialization.Serializable
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class NoteManager(private val context: Context) : DataHolderList<Note>() {
    private val dataDirectory = SavedDataDirectory(this.context.getString(R.string.notes_directory_name), context)
    private val notes = dataDirectory.getData<Note>()
    private val idAllocator = IntAllocator(this.getNoteIds())

    override fun retrieveContent(): LinkedList<Note> {
        return notes
    }
    override fun save(element: Note) {
        if (element.id == null) {
            element.id = idAllocator.allocate()
            element.title += element.id
        }
        this.dataDirectory.saveData(element)
    }
    override fun delete(element: Note) {
        if (element.id == null) return
        this.idAllocator.release(element.id!!)
        this.dataDirectory.saveData(null, element.id!!)
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

@Serializable
data class Note(var title: String, val lines: List<NotePart> = LinkedList()) : DataHolder {
    override var id: Int? = null
    override val creationStamp: Long = Date().time
}

@Serializable
data class NotePart(val type: String, val content: String)