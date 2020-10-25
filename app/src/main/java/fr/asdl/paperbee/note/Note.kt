package fr.asdl.paperbee.note

import fr.asdl.paperbee.IntAllocator
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

    /**
     * Moves a part and all its children.
     * Use this method after [expand] or ensure all children are visible (you can call [expand]
     * before in order to assert that)
     */
    fun movePart(notePart: NotePart?, of: Int) {
        if (notePart == null) return
        notePart.getChildren().forEach {
            val order = this.getOrder(it)
            this.move(order, order + of)
            movePart(it, of)
        }
    }

}