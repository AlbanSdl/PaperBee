package fr.asdl.minder.note.bindings

import android.view.View
import android.widget.TextView
import fr.asdl.minder.R
import fr.asdl.minder.note.Notable
import fr.asdl.minder.note.NoteFolder
import fr.asdl.minder.note.NoteManager
import fr.asdl.minder.view.tree.TreeNode

class NotableTree(private val current: Notable<*>, private val listener: (Notable<*>) -> Unit, layer: Notable<*>) :
    TreeNode<Notable<*>>(layer) {

    constructor(current: Notable<*>, listener: (Notable<*>) -> Unit):
            this(current, listener, current.noteManager!!.findElementById(NoteManager.ROOT_ID) as NoteFolder)

    init {
        this.t.getRawContents().filterIsInstance<Notable<*>>().forEach {
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