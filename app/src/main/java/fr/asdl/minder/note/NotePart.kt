package fr.asdl.minder.note

import fr.asdl.minder.view.sentient.DataHolder
import kotlinx.serialization.Serializable


/**
 * A part of a note. This can be text, image, checkbox-indented item, or whatever extends
 * this sealed class. It must be displayable by the Note and its recycler view.
 */
@Serializable
sealed class NotePart(override val id: Int = 0, override val creationStamp: Long = 0L) : DataHolder

interface TextNotePart {
    var content: String
}

interface CheckableNotePart {
    var checked: Boolean
}

@Serializable
class NoteText(override var content: String) : NotePart(), TextNotePart

@Serializable
class NoteCheckBoxable(override var content: String, override var checked: Boolean) : NotePart(), TextNotePart, CheckableNotePart