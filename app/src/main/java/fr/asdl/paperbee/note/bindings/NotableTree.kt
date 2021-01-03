package fr.asdl.paperbee.note.bindings

import android.view.View
import android.widget.TextView
import fr.asdl.paperbee.R
import fr.asdl.paperbee.note.Notable
import fr.asdl.paperbee.note.NoteFolder
import fr.asdl.paperbee.storage.DatabaseProxy.Companion.ROOT_ID
import fr.asdl.paperbee.view.tree.TreeNode

class NotableTree(private val current: Notable<*>, private val listener: (Notable<*>) -> Unit, layer: Notable<*>) :
    TreeNode<Notable<*>>(layer) {

    constructor(current: Notable<*>, listener: (Notable<*>) -> Unit):
            this(current, listener, current.db!!.findElementById(ROOT_ID) as NoteFolder)

    init {
        this.t.filtered.contents.filterIsInstance<Notable<*>>().forEach {
            this.append(NotableTree(current, this.listener, it))
        }
        if (current.isChildOf(layer.id!!))
            this.toggleExpansion()
    }

    override fun getLayoutId(): Int {
        return R.layout.directory_notable_view
    }

    override fun onCreateView(view: View) {
        val textView = view.findViewById<TextView>(R.id.directory_name)
        textView.text = this.t.title
        textView.setOnClickListener { listener.invoke(this@NotableTree.t) }
    }

    override fun allowExpand(): Boolean {
        return true
    }
}