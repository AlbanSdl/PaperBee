package fr.asdl.minder.note

import android.content.Context
import fr.asdl.minder.R
import fr.asdl.minder.preferences.SavedDataDirectory
import fr.asdl.minder.view.DataHolder
import fr.asdl.minder.view.DataHolderList
import kotlinx.serialization.Serializable
import java.util.*

class NoteManager(private val context: Context) : DataHolderList<Note>() {
    private val notes = SavedDataDirectory(this.context.getString(R.string.notes_directory_name), context).getData<Note>()
    override fun retrieveContent(): LinkedList<Note> {
        return notes
    }
}

@Serializable
data class Note(val id: Int, val name: String, val lines: List<NotePart> = LinkedList()) : DataHolder

@Serializable
data class NotePart(val type: String, val content: String)