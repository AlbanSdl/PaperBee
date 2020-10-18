package fr.asdl.minder.note

import android.util.Log
import fr.asdl.minder.IntAllocator
import fr.asdl.minder.view.sentient.ModificationType
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

    private val filtered = arrayListOf<Int>()

    override fun shouldEnforceParentId(): Boolean = false

    /**
     * Retrieves the full content of the note, including the hidden NoteParts
     * (filtered via [collapse])
     */
    fun getRawContents(): List<NotePart> {
        return super.getContents()
    }

    override fun getContents(): List<NotePart> {
        return super.getContents().filter { it.id !in filtered }
    }

    /**
     * Hides all the sub-elements contained in a NotePart
     */
    fun collapse(notePart: NotePart?) {
        if (notePart == null) return
        notePart.getChildren().forEach {
            this.collapse(it)
            Log.e(javaClass.simpleName, "Child: ${(it as TextNotePart).content}")
            val index = this.getOrder(notePart)
            this.onChange(ModificationType.REMOVAL, index + 1, null)
            this.filtered.add(it.id!!)
        }
    }

    /**
     * Shows all the sub-elements contained in a NotePart
     */
    fun expand(notePart: NotePart?) {
        if (notePart == null) return
        notePart.getChildren().forEach {
            this.filtered.remove(it.id!!)
            this.onChange(ModificationType.ADDITION, this.getOrder(notePart) + 1, null)
            this.expand(it)
        }
    }

}