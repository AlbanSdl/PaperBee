package fr.asdl.minder.note

import fr.asdl.minder.exceptions.NotePartAttachmentException
import fr.asdl.minder.view.sentient.DataHolder
import fr.asdl.minder.view.sentient.DataHolderList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * A part of a note. This can be text, image, checkbox-indented item, or whatever extends
 * this sealed class. It must be displayable by the Note and its recycler view.
 */
@Serializable
sealed class NotePart(override var id: Int? = null, override var order: Int = -1,
                      @Transient override var noteManager: NoteManager? = null) : DataHolder {

    override fun getParent(): DataHolderList<*>? = this.getMixedParent() as? DataHolderList<*>
    fun getMixedParent(): DataHolder? = noteManager?.findElementById(this.parentId)

    /**
     * Returns whether the NotePart has elements above it in the same note.
     */
    fun hasAbove(): Boolean {
        return try {
            val above = this.getAbove()
            above.id != this.parentId
        } catch (ex: NotePartAttachmentException) {
            false
        }
    }

    /**
     * Returns the depth of the NotePart (ie. the number of NotePart to access to reach the note)
     * so that this method returns 0 if the element is just a child of the note
     */
    fun getDepth(): Int {
        val parent = this.getParentPart()
        if (parent is NotePart) return parent.getDepth() + 1
        return 0
    }

    /**
     * Returns the note in which the NotePart is contained.
     * Can be null if the NotePart has no been attached to any note or if it has been detached
     */
    private fun getNote(): Note? {
        return this.getParent() as? Note ?: this.getParentPart()?.getNote()
    }

    /**
     * Attaches the current note to the given NotePart. This can be only be done if the NoteParts
     * have BOTH been attached to the same Note.
     * @param notePart the [NotePart] which will be the parent of the current one.
     * @throws NotePartAttachmentException if the two NoteParts are not in the same Note or
     * if there are other items between them which are not belonging to the chosen notePart
     */
    fun attachToPart(notePart: DataHolder) {
        if (this.parentId == notePart.id) return
        val note = this.getNote()
        if (note != null) {
            if (notePart is NotePart) {
                if (notePart !in note.getContents())
                    throw NotePartAttachmentException(this, notePart,
                        NotePartAttachmentException.Reason.DIFFERENT_NOTE)
                if (notePart.getDepth() > this.getDepth())
                    for (i in notePart.order + 1 until this.order)
                        if (note.getContents()[i].parentId != notePart.id)
                            throw NotePartAttachmentException(this, notePart,
                                NotePartAttachmentException.Reason.FOREIGN_ELEMENTS)
            }
            this.parentId = notePart.id
            fun update(nPart: NotePart) {
                note.update(nPart)
                nPart.getChildren().forEach { update(it) }
            }
            update(this)
        }
    }

    /**
     * Retrieves the note part above the current one.
     * @throws NotePartAttachmentException if the note part is not attached to any note or
     * if there is no note above.
     */
    fun getAbove(): NotePart {
        val note = getNote() ?: throw NotePartAttachmentException(this, this,
            NotePartAttachmentException.Reason.NO_NOTE_ABOVE)
        if (note.getContents().size <= this.order || this.order <= 0) throw NotePartAttachmentException(this, this,
            NotePartAttachmentException.Reason.NO_NOTE_ABOVE)
        return note.getContents()[this.order - 1]
    }

    fun getParentPart(): NotePart? = this.getMixedParent() as? NotePart

    private fun getChildren(): List<NotePart> {
        return this.getNote()?.getContents()?.filter { it.parentId == this.id } ?: listOf()
    }

}

interface TextNotePart {
    var content: String
}

interface CheckableNotePart {
    var checked: Boolean
}

@Serializable
class NoteText(override var content: String,
               override var parentId: Int? = null) : NotePart(), TextNotePart

@Serializable
class NoteCheckBoxable(override var content: String,
                       override var checked: Boolean,
                       override var parentId: Int? = null) : NotePart(), TextNotePart, CheckableNotePart