package fr.asdl.minder.note

import fr.asdl.minder.view.sentient.DataHolder
import kotlinx.serialization.Serializable


/**
 * A part of a note. This can be text, image, checkbox-indented item, or whatever implements
 * this interface. It must be displayable by the Note and its recycler view.
 */
@Serializable
sealed class NotePart(override val id: Int = 0, override val creationStamp: Long = 0L) : DataHolder

@Serializable
class NoteText(val content: String) : NotePart()

@Serializable
class NoteCheckBoxable(val content: String) : NotePart()