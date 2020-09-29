package fr.asdl.minder.note

import fr.asdl.minder.IntAllocator
import java.util.*

open class NoteFolder(title: String,
                      noteManager: NoteManager?,
                      override var contents: LinkedList<Notable<*>> = LinkedList(),
                      idAllocator: IntAllocator?,
                      parentId: Int?
) : Notable<Notable<*>>(title, noteManager, contents, idAllocator, parentId) {

    override fun save(element: Notable<*>): Boolean {
        noteManager?.save(element)
        return true
    }

    override fun delete(element: Notable<*>, oldId: Int): Boolean {
        noteManager?.delete(element, oldId)
        return true
    }

    override fun shouldNotify(): Boolean = true

}