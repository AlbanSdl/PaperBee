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
    private val notes = LinkedList<Note>()
    private val idAllocator = IntAllocator(this.getNoteIds())

    init {
        dataDirectory.loadData(this)
    }

    override fun retrieveContent(): LinkedList<Note> {
        return notes
    }
    override fun save(element: Note) {
        if (element.id == null)
            element.id = idAllocator.allocate()
        this.dataDirectory.saveDataAsync(element)
    }
    override fun delete(element: Note) {
        if (element.id == null) return
        this.idAllocator.release(element.id!!)
        this.dataDirectory.saveDataAsync(null, element.id!!)
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

/**
 * The structure of a note, containing a title and contents.
 *
 * This will extend DataHolderList<NotePart> in the future and will be displayed
 * with a recycler view ↓
 */
@Serializable
data class Note(var title: String, val contents: List<NotePart> = LinkedList()) : DataHolder {
    override var id: Int? = null
    override val creationStamp: Long = Date().time
}

/**
 * A part of a note. This can be text, image, checkbox-indented item, or whatever implements
 * this interface. It must be displayable by the Note and its recycler view.
 *
 * This will implement DataHolder in the future and will be displayed in the recycler view of
 * its Note ↑
 */
interface NotePart

@Serializable
data class NoteText(val content: String) : NotePart

@Serializable
data class NoteImage(val src: String) : NotePart