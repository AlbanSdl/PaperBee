package fr.asdl.minder.note

import fr.asdl.minder.IntAllocator
import fr.asdl.minder.view.sentient.DataHolder
import fr.asdl.minder.view.sentient.DataHolderList
import java.util.*

abstract class Notable<T : DataHolder>(
    var title: String,
    noteManager: NoteManager?,
    content: LinkedList<T> = LinkedList(),
    idAllocator: IntAllocator?,
    parentId: Int?
) : DataHolder, DataHolderList<T>(idAllocator, noteManager, parentId) {

    override var id: Int? = null
    override var order: Int = -1
    override val contents: LinkedList<T> = content
    var notify: Boolean = false

    override fun shouldNotify(): Boolean = notify

}