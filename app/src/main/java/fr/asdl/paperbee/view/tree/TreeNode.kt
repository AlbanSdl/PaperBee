package fr.asdl.paperbee.view.tree

import android.view.View
import androidx.annotation.LayoutRes
import fr.asdl.paperbee.IntAllocator
import java.util.*

abstract class TreeNode<T>(protected val t: T) {

    private var expanded: Boolean = false
    private val children: LinkedList<TreeNode<T>> = LinkedList()
    private var parent: TreeNode<T>? = null
    private var id: Int = -1

    fun toggleExpansion(): Boolean {
        if (this.canExpand())
            this.expanded = !this.expanded
        return this.expanded
    }

    infix fun contains(other: TreeNode<T>): Boolean {
        for (i in children) {
            if (i == other) return true
            if (i contains other) return true
        }
        return false
    }

    fun append(vararg item: TreeNode<T>) {
        listOf(*item).forEach {
            if (this contains it || this == it)
                throw Exception("Unable to append node $it in one of its children !")
            this.children.add(it)
            it.parent = this
        }
    }

    fun onAttached(intAllocator: IntAllocator) {
        this.id = intAllocator.allocate()
        for (i in children) i.onAttached(intAllocator)
    }

    fun getChildCount(): Int = this.children.size
    fun getChildAt(index: Int): TreeNode<T> = this.children[index]
    fun getParent(): TreeNode<T>? = this.parent
    fun getId(): Int = this.id
    fun isExpanded(): Boolean = this.expanded
    fun canExpand(): Boolean = this.getChildCount() > 0 && this.allowExpand()
    fun getDepth(): Int {
        var depth = 0
        var elem: TreeNode<T>? = this
        while (elem?.parent != null) {
            elem = elem.parent
            depth++
        }
        return depth
    }

    @LayoutRes
    abstract fun getLayoutId(): Int
    abstract fun onCreateView(view: View)
    abstract fun allowExpand(): Boolean

}