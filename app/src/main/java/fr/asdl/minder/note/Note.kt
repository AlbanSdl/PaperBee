package fr.asdl.minder.note

import fr.asdl.minder.IntAllocator
import java.util.*

/**
 * The structure of a note, containing a title and contents.
 */
class Note(title: String,
           noteManager: NoteManager?,
           content: LinkedList<NotePart> = LinkedList(),
           idAllocator: IntAllocator?,
           parentId: Int?
) : Notable<NotePart>(title, noteManager, content, idAllocator, parentId) {

    override fun shouldEnforceParentId(): Boolean = false

}