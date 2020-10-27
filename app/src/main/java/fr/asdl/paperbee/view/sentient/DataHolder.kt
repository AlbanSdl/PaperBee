package fr.asdl.paperbee.view.sentient

import fr.asdl.paperbee.note.NoteManager
import kotlinx.serialization.Transient

/**
 * The base class for any item contained in a [DataHolderList]. This is useful for any element
 * to display in a [SentientRecyclerViewAdapter]
 */
interface DataHolder {
    /**
     * The id of the [DataHolder].
     */
    var id: Int?
    /**
     * The order of the [DataHolder] in its [DataHolderList]
     * Default (unset) value must be negative.
     */
    var order: Int
    /**
     * The id of the [DataHolderList] which contains this [DataHolder]
     * If this value is null, it means that this [DataHolder] has not been attached to any parent
     * or that it is contained in the note manager (in the root view)
     */
    var parentId: Int?
    /**
     * The note manager this [DataHolder] is registered in. Can be used to retrieve the parent
     */
    @Transient
    var noteManager: NoteManager?

    /**
     * Retrieves the [DataHolderList] which contains this [DataHolder].
     * This value can be null if the [DataHolder] has not been attached to any parent.
     */
    fun getParent(): DataHolderList<*>?
}
