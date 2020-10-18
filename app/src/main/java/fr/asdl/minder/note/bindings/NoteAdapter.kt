package fr.asdl.minder.note.bindings

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import fr.asdl.minder.R
import fr.asdl.minder.activities.MainActivity
import fr.asdl.minder.note.*
import fr.asdl.minder.note.NoteManager.Companion.TRASH_ID
import fr.asdl.minder.view.sentient.SentientRecyclerView
import fr.asdl.minder.view.sentient.SentientRecyclerViewAdapter
import fr.asdl.minder.view.tree.TreeView

class NoteAdapter(private val folder: NoteFolder) : SentientRecyclerViewAdapter<Notable<*>>(folder) {

    override fun getLayoutId(): Int {
        return R.layout.notes_layout
    }

    override fun onBindViewHolder(holder: ViewHolder, content: Notable<*>) {
        // We set the background tint if the notable has a color
        holder.itemView.findViewById<CardView>(R.id.note_element).setBackgroundColor(
            ContextCompat.getColor(holder.itemView.context!!,
                if (content.color != null) content.color!!.id else R.color.blank))
        // We set the note contents (title, etc)
        (holder.findViewById(R.id.note_title)!! as TextView).text = content.title
        this.setTransitionNames(holder, content)
        val rec = (holder.findViewById(R.id.note_elements_recycler) as SentientRecyclerView)
        if (content.getContents().isNotEmpty() && content is Note) {
            rec.visibility = View.VISIBLE
            rec.addItemDecoration(NotePartDecoration())
            rec.adapter = NotePartAdapterStatic(content, holder.findViewById(R.id.note_element) as View)
            rec.addTouchDelegation()
        } else {
            rec.visibility = View.GONE
        }
        if (folder.id != TRASH_ID) {
            (holder.findViewById(R.id.note_element) as View).setOnClickListener { (holder.itemView.context as MainActivity).openNotable(
                content,
                it,
                holder.findViewById(R.id.note_title) as View,
                rec
            )}
        } else {
            (holder.findViewById(R.id.note_element) as View).setOnClickListener {  Toast.makeText(holder.itemView.context, R.string.trash_cannot_edit, Toast.LENGTH_SHORT).show() }
        }
        // We display the folder icon if content is a folder
        val fold = holder.findViewById(R.id.note_folder_icon)
        if (content is NoteFolder)
            fold?.visibility = View.VISIBLE
        else
            fold?.visibility = View.GONE
    }

    private fun setTransitionNames(holder: ViewHolder, content: Notable<*>) {
        val transitionComponents = listOf(holder.findViewById(R.id.note_element), holder.findViewById(R.id.note_title), holder.findViewById(R.id.note_elements_recycler))
        for (view in transitionComponents)
            if (view != null)
                ViewCompat.setTransitionName(view, "${ViewCompat.getTransitionName(view)}#${content.id}")
    }
    
    @SuppressLint("InflateParams")
    override fun onSwipeLeft(context: Context, content: Notable<*>) {
        val treeView = LayoutInflater.from(context).inflate(R.layout.treeview_layout, null) as TreeView
        val dialog = AlertDialog.Builder(context).setTitle(R.string.notable_move).apply {
            setView(treeView)
            setNegativeButton(android.R.string.cancel) { display, _ -> display.cancel() }
            setOnCancelListener {
                this@NoteAdapter.getDataHolder().update(content)
            }
        }.create()
        val adapter = DirectoryTree(content) {
            this@NoteAdapter.folder.remove(content, false)
            it.add(content)
            dialog.dismiss()
        }
        treeView.attachData(adapter)
        dialog.show()
    }

    override fun onSwipeRight(context: Context, content: Notable<*>) {
        if (folder.id != TRASH_ID) {
            val trash = this.getDataHolder().noteManager?.findElementById(TRASH_ID) as? NoteFolder
            this.getDataHolder().remove(content, false)
            trash?.add(content)
            if (content is NoteFolder) content.getContents().forEach { onSwipeRight(context, it) }
        } else {
            AlertDialog.Builder(context).setTitle(R.string.trash_delete).setMessage(R.string.trash_delete_details).apply {
                setPositiveButton(android.R.string.ok) { _, _ -> this@NoteAdapter.getDataHolder().remove(content) }
                setNegativeButton(android.R.string.cancel) { display, _ -> display.cancel() }
                setOnCancelListener { this@NoteAdapter.getDataHolder().update(content) }
            }.show()
        }
    }

}