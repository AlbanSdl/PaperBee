package fr.asdl.minder.note

import fr.asdl.minder.view.sentient.DataHolder
import fr.asdl.minder.view.sentient.DataHolderList
import java.util.*

/**
 * The structure of a note, containing a title and contents.
 */
class Note(var title: String,
           content: LinkedList<NotePart> = LinkedList(),
           var noteManager: DataHolderList<Note>?,
) : DataHolder, DataHolderList<NotePart>() {

    override var id: Int? = null
    override var order: Int = -1
    override val contents: LinkedList<NotePart> = content

    fun save() {
        if (this.noteManager != null)
            if (this.id == null) noteManager!!.add(this) else noteManager!!.update(this, false)
    }

    override fun save(element: NotePart) {
        this.save()
    }

    override fun delete(element: NotePart) {
        this.save()
    }

    override fun shouldNotify(): Boolean {
        return true
    }
}