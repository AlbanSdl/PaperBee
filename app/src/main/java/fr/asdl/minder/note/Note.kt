package fr.asdl.minder.note

import fr.asdl.minder.IntAllocator
import fr.asdl.minder.view.sentient.DataHolder
import fr.asdl.minder.view.sentient.DataHolderList
import java.util.*

/**
 * The structure of a note, containing a title and contents.
 */
class Note(var title: String,
           noteManager: NoteManager?,
           content: LinkedList<NotePart> = LinkedList(),
           idAllocator: IntAllocator?,
           parentId: Int?
) : DataHolder, DataHolderList<NotePart>(idAllocator, noteManager, parentId) {

    override var id: Int? = null
    override var order: Int = -1
    override val contents: LinkedList<NotePart> = content
    var notify: Boolean = false

    override fun shouldNotify(): Boolean = notify

}