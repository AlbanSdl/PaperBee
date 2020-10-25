package fr.asdl.minder.note.bindings

import android.view.View
import android.widget.TextView
import fr.asdl.minder.R
import fr.asdl.minder.note.Notable
import fr.asdl.minder.note.NoteFolder
import fr.asdl.minder.note.NoteManager
import fr.asdl.minder.view.tree.TreeNode

class DirectoryTree(private val current: Notable<*>?, private val listener: (NoteFolder) -> Unit, folder: NoteFolder) :
    TreeNode<NoteFolder>(folder) {

    constructor(current: Notable<*>, listener: (NoteFolder) -> Unit):
            this(current, listener, current.noteManager!!.findElementById(NoteManager.ROOT_ID) as NoteFolder)

    init {
        this.t.getContents().filterIsInstance<NoteFolder>().forEach {
            if (it != current) this.append(DirectoryTree(current, this.listener, it))
        }
        if (current?.isChildOf(folder.id!!) == true)
            this.toggleExpansion()
    }

    override fun getLayoutId(): Int {
        return R.layout.directory_view
    }

    override fun onCreateView(view: View) {
        val textView = view.findViewById<TextView>(R.id.directory_name)
        textView.text = this.t.title
        textView.setOnClickListener { listener.invoke(this@DirectoryTree.t) }
    }

    override fun allowExpand(): Boolean {
        return true
    }
}