package fr.asdl.paperbee.note

import java.util.*

/**
 * The structure of a note, containing a title and contents.
 */
class Note: Notable<NotePart>() {

    override val contents: LinkedList<NotePart>
        get() = db?.findNoteContent(this.id!!) ?: LinkedList()

    /**
     * Hides all the sub-elements contained in a NotePart
     */
    fun collapse(notePart: NotePart?) {
        if (notePart == null) return
        notePart.getChildren().forEach {
            this.collapse(it)
            this.hide(it)
        }
    }

    /**
     * Shows all the sub-elements contained in a NotePart
     */
    fun expand(notePart: NotePart?) {
        if (notePart == null) return
        notePart.getChildren().forEach {
            this.show(it)
            this.expand(it)
        }
    }

    override fun move(fromPos: Int, toPos: Int) {
        val part = this.filtered[fromPos]
        val previousOrder = part.order
        this.moveIndices(part.order, this.filtered[toPos].order)
        val translationOf = part.order - previousOrder
        fun mI(p: NotePart) {
            if (p != part)
                this.moveIndices(p.order, p.order + translationOf)
            p.getChildren().forEach { mI(it) }
        }
        mI(part)
    }

}