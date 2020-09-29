package fr.asdl.minder.note

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import fr.asdl.minder.R
import fr.asdl.minder.activities.MainActivity
import fr.asdl.minder.note.NoteManager.Companion.TRASH_ID
import fr.asdl.minder.view.sentient.SentientRecyclerView
import fr.asdl.minder.view.sentient.SentientRecyclerViewAdapter

class NoteAdapter(private val folder: NoteFolder) : SentientRecyclerViewAdapter<Notable<*>>(folder) {

    override fun getLayoutId(): Int {
        return R.layout.notes_layout
    }

    override fun onBindViewHolder(holder: ViewHolder, content: Notable<*>) {
        (holder.findViewById(R.id.note_title)!! as TextView).text = content.title
        this.setTransitionNames(holder, content)
        val rec = (holder.findViewById(R.id.note_elements_recycler) as SentientRecyclerView)
        if (content.getContents().isNotEmpty() && content is Note) {
            rec.visibility = View.VISIBLE
            rec.adapter = NotePartAdapterInList(content, holder.findViewById(R.id.note_element) as View)
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

    abstract class NotePartAdapter(private val note: Note) : SentientRecyclerViewAdapter<NotePart>(note) {

        override fun onBindViewHolder(holder: ViewHolder, content: NotePart) {
            // TextNoteParts
            val textView = (holder.findViewById(R.id.note_text) as TextView)
            if (content is TextNotePart) {
                textView.text = content.content
                textView.visibility = View.VISIBLE
            }
            else textView.visibility = View.GONE
            // CheckableNotePart
            val checkBox = (holder.findViewById(R.id.note_checkbox) as CheckBox)
            if (content is CheckableNotePart) {
                checkBox.isChecked = content.checked
                checkBox.visibility = View.VISIBLE
                if (this.note.id != TRASH_ID)
                    checkBox.setOnClickListener { content.checked = checkBox.isChecked; this.getDataHolder().update(content) }
                else
                    checkBox.isEnabled = false
            } else {
                checkBox.visibility = View.GONE
                checkBox.setOnClickListener(null)
            }
        }

    }

    inner class NotePartAdapterInList(note: Note, private val clickDelegateView: View? = null) : NotePartAdapter(note) {

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: ViewHolder, content: NotePart) {
            super.onBindViewHolder(holder, content)
            if (clickDelegateView != null) {
                holder.itemView.setOnTouchListener { v, e ->
                    val rect = Rect()
                    val rect2 = Rect()
                    v.getGlobalVisibleRect(rect)
                    clickDelegateView.getGlobalVisibleRect(rect2)
                    clickDelegateView.onTouchEvent(MotionEvent.obtain(e.downTime, e.eventTime, e.action,
                        e.x + rect.left - rect2.left,
                        e.y + rect.top - rect2.top,
                        e.metaState))
                    false
                }
                holder.itemView.isClickable = true
            }
        }

        override fun getLayoutId(): Int {
            return R.layout.note_part_layout
        }

    }
    
    override fun onSwipeLeft(context: Context, content: Notable<*>) {
        AlertDialog.Builder(context).setTitle(R.string.notable_move).apply {
            val adapter = SwipeMoveDirectoryList(context, content)
            setAdapter(adapter) { _, which ->
                val destination = adapter.getItem(which)
                this@NoteAdapter.folder.remove(content, false)
                destination.folder.add(content)
            }
            setNegativeButton(android.R.string.cancel) { display, _ -> display.cancel() }
            setOnCancelListener {
                this@NoteAdapter.getDataHolder().update(content)
            }
        }.show()
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

    private class SwipeMoveDirectoryList(context: Context, notable: Notable<*>) : BaseAdapter() {
        data class Directory(val name: String, val folder: NoteFolder)
        private val directories = arrayListOf<Directory>()
        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        init {
            val parent = notable.getParent()
            fun findDirectory(folder: Notable<*>, directory: String) {
                if (folder is NoteFolder && folder != notable) {
                    val dir = "$directory/${folder.title}"
                    if (folder != parent) directories.add(Directory(dir, folder))
                    folder.getContents().forEach { findDirectory(it, dir) }
                }
            }
            findDirectory(notable.noteManager!!, "")
            directories.sortBy { it.name }
        }

        override fun getCount(): Int {
            return this.directories.size
        }

        override fun getItem(position: Int): Directory {
            return this.directories[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: inflater.inflate(R.layout.directory_view, parent, false)
            view.findViewById<TextView>(R.id.directory_name).text = getItem(position).name
            return view
        }
    }

}