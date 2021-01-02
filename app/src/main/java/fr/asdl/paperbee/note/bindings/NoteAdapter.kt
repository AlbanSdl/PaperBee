package fr.asdl.paperbee.note.bindings

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
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.MainActivity
import fr.asdl.paperbee.note.*
import fr.asdl.paperbee.storage.DatabaseProxy.Companion.TRASH_ID
import fr.asdl.paperbee.view.sentient.SentientRecyclerView
import fr.asdl.paperbee.view.sentient.SentientRecyclerViewAdapter
import fr.asdl.paperbee.view.tree.TreeView

class NoteAdapter(private val folder: NoteFolder) : SentientRecyclerViewAdapter<Notable<*>, Any>(folder) {

    override fun getLayoutId(): Int {
        return R.layout.notes_layout
    }

    override fun onBindViewHolder(holder: ViewHolder<Any>, content: Notable<*>) {
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

    private fun setTransitionNames(holder: ViewHolder<Any>, content: Notable<*>) {
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
                this@NoteAdapter.getDataHolder().notifyUpdated(content)
            }
        }.create()
        val adapter = DirectoryTree(content, {
            this@NoteAdapter.folder.remove(content)
            it.add(content)
            dialog.dismiss()
        })
        treeView.attachData(adapter)
        dialog.show()
    }

    override fun onSwipeRight(context: Context, content: Notable<*>) {
        if (folder.id != TRASH_ID) {
            val trash = this.getDataHolder().db?.findElementById(TRASH_ID) as? NoteFolder
            this.getDataHolder().remove(content)
            trash?.add(content)
            if (content is NoteFolder) content.getContents().forEach { onSwipeRight(context, it) }
        } else {
            AlertDialog.Builder(context).setTitle(R.string.trash_delete).setMessage(R.string.trash_delete_details).apply {
                setPositiveButton(android.R.string.ok) { _, _ -> this@NoteAdapter.getDataHolder().remove(content); content.db?.delete(content) }
                setNegativeButton(android.R.string.cancel) { display, _ -> display.cancel() }
                setOnCancelListener { this@NoteAdapter.getDataHolder().notifyUpdated(content) }
            }.show()
        }
    }

}