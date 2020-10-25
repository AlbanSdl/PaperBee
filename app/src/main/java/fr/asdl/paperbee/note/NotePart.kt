package fr.asdl.paperbee.note

import fr.asdl.paperbee.exceptions.NotePartAttachmentException
import fr.asdl.paperbee.view.sentient.DataHolder
import fr.asdl.paperbee.view.sentient.DataHolderList
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
    private fun getMixedParent(): DataHolder? = noteManager?.findElementById(this.parentId)

    /**
     * Returns whether the [NotePart] with id [partId] is a parent of the current note.
     */
    private fun hasParent(partId: Int): Boolean {
        if (this.parentId == partId) return true
        return this.getParentPart()?.hasParent(partId) ?: false
    }

    /**
     * Returns whether the NotePart has elements above it in the same note.
     */
    fun hasAbove(): Boolean {
        val id = this.getAbove()?.id
        return id != null && id != this.parentId
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
     * Moves the NotePart in the NotePart above
     * @throws NotePartAttachmentException if the operation cannot succeed (eg. if there is no
     * note above or if the current NotePart is not in a note)
     */
    fun moveIn() {
        this.attachToPart(this.getAbove() ?: throw NotePartAttachmentException(this, this, NotePartAttachmentException.Reason.NO_NOTE_ABOVE))
    }

    /**
     * Moves the NotePart out of its parent if there is one (it will not be removed from the note,
     * even if the only parent is the note)
     * @throws NotePartAttachmentException if the operation cannot succeed (eg. if there is no
     * parent (as a NotePart) or if the current NotePart is not in a note)
     */
    fun moveOut() {
        val previousParent = this.getParentPart()!!
        this.attachToPart(previousParent.getMixedParent()!!)
        // We move the current NotePart after its old parent group
        val note = getNote()!!
        var moveTo = this.order
        for (i in this.order + 1 until note.getRawContents().size) {
            if (!note.getRawContents()[i].hasParent(previousParent.id!!))
                break
            moveTo++
        }
        note.move(this.order, moveTo)
    }

    /**
     * Computes the parent of the current [NotePart] depending on the elements above and below.
     * Indents the part depending on their lowest indentation.
     */
    fun updateParentId(): Boolean {
        val above = this.getAbove()
        val below = this.getBelow()
        val aDepth = above?.getDepth()
        val bDepth = below?.getDepth()
        this.parentId = if (aDepth != null && bDepth != null) {
            when {
                // We're in a group
                below.hasParent(above.id!!) -> above.id!!
                // We choose the lowest depth
                aDepth > bDepth -> below.parentId!!
                else -> above.parentId!!
            }
        } else if (aDepth != null || bDepth != null) {
            // find the one which is not null and use the same parent
            if (aDepth != null) above.parentId!! else below!!.parentId!!
        } else {
            // use note as parent
            getNote()?.id ?: throw NotePartAttachmentException(this, this,
                NotePartAttachmentException.Reason.NOT_IN_NOTE)
        }
        return true
    }

    /**
     * Attaches the current note to the given NotePart. This can be only be done if the NoteParts
     * have BOTH been attached to the same Note.
     * @param notePart the [NotePart] which will be the parent of the current one.
     * @throws NotePartAttachmentException if the two NoteParts are not in the same Note or
     * if there are other items between them which are not belonging to the chosen notePart
     */
    private fun attachToPart(notePart: DataHolder) {
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
     * Retrieves the note part above the current one. Can be null
     */
    private fun getAbove(): NotePart? {
        val note = getNote() ?: return null
        if (note.getContents().size <= this.order || this.order <= 0) return null
        return note.getContents()[this.order - 1]
    }

    /**
     * Retrieves the note part below the current one. Can be null
     */
    private fun getBelow(): NotePart? {
        val note = getNote() ?: return null
        if (note.getContents().size <= this.order + 1 || this.order < 0) return null
        return note.getContents()[this.order + 1]
    }

    fun getParentPart(): NotePart? = this.getMixedParent() as? NotePart

    fun getChildren(): List<NotePart> {
        return this.getNote()?.getRawContents()?.filter { it.parentId == this.id } ?: listOf()
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