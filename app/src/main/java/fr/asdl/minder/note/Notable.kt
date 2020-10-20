package fr.asdl.minder.note

import fr.asdl.minder.IntAllocator
import fr.asdl.minder.view.options.Color
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
    var color: Color? = null

    override fun shouldNotify(): Boolean = notify

    fun isChildOf(parentId: Int): Boolean {
        if (this.id == parentId) return true
        return (this.getParent() as? Notable<*>)?.isChildOf(parentId) == true
    }
}