package fr.asdl.paperbee.note.bindings

import android.view.View
import android.widget.TextView
import fr.asdl.paperbee.R
import fr.asdl.paperbee.note.Notable
import fr.asdl.paperbee.note.NoteFolder
import fr.asdl.paperbee.storage.DatabaseProxy.Companion.ROOT_ID
import fr.asdl.paperbee.view.tree.TreeNode

class DirectoryTree(private val current: Notable<*>, private val listener: (NoteFolder) -> Unit, folder: NoteFolder,
                    private val textSize: Float? = null
) :
    TreeNode<NoteFolder>(folder) {

    constructor(current: Notable<*>, listener: (NoteFolder) -> Unit, textSize: Float? = null):
            this(current, listener, current.db!!.findElementById(ROOT_ID) as NoteFolder, textSize)

    init {
        this.t.getContents().filterIsInstance<NoteFolder>().forEach {
            if (it != current) this.append(DirectoryTree(current, this.listener, it, textSize))
        }
        if (current.isChildOf(folder.id!!))
            this.toggleExpansion()
    }

    override fun getLayoutId(): Int {
        return R.layout.directory_view
    }

    override fun onCreateView(view: View) {
        val textView = view.findViewById<TextView>(R.id.directory_name)
        textView.text = this.t.title
        if (textSize != null) textView.textSize = textSize
        textView.setOnClickListener { listener.invoke(this@DirectoryTree.t) }
    }

    override fun allowExpand(): Boolean {
        return true
    }
}