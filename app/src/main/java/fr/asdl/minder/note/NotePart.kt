package fr.asdl.minder.note

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

    override fun getParent(): DataHolderList<*>? = noteManager?.findElementById(this.parentId) as DataHolderList<*>

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