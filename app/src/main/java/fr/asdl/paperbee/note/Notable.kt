package fr.asdl.paperbee.note

import fr.asdl.paperbee.view.options.Color
import fr.asdl.paperbee.view.sentient.DataHolder
import fr.asdl.paperbee.view.sentient.DataHolderList

abstract class Notable<T : DataHolder> : DataHolderList<T>() {

    var title: String = ""
    var color: Color? = null

    fun isChildOf(parentId: Int): Boolean {
        if (this.id == parentId) return true
        return (this.getParent() as? Notable<*>)?.isChildOf(parentId) == true
    }
}