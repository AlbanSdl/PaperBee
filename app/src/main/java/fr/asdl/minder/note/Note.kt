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

    /**
     * Hides all the sub-elements contained in a NotePart
     */
    fun collapse(notePart: NotePart?) {
        if (notePart == null) return
        notePart.getChildren().forEach {
            this.collapse(it)
            this.hide(it)
        }
    }

    /**
     * Shows all the sub-elements contained in a NotePart
     */
    fun expand(notePart: NotePart?) {
        if (notePart == null) return
        notePart.getChildren().forEach {
            this.show(it)
            this.expand(it)
        }
    }

}