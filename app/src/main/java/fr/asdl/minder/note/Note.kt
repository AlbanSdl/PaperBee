package fr.asdl.minder.note

import fr.asdl.minder.IntAllocator
import fr.asdl.minder.view.sentient.DataHolder
import fr.asdl.minder.view.sentient.DataHolderList
import java.util.*

/**
 * The structure of a note, containing a title and contents.
 */
class Note(var title: String,
           var noteManager: DataHolderList<Note>?,
           content: LinkedList<NotePart> = LinkedList(),
           idAllocator: IntAllocator?
) : DataHolder, DataHolderList<NotePart>(idAllocator) {

    override var id: Int? = null
    override var order: Int = -1
    override val contents: LinkedList<NotePart> = content

    fun save() {
        if (this.noteManager != null)
            if (this.id == null) noteManager!!.add(this) else noteManager!!.update(this, false)
    }

    override fun save(element: NotePart) {
    }

    override fun delete(element: NotePart, oldId: Int) {
    }

    override fun shouldNotify(): Boolean = true

}