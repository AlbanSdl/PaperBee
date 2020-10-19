package fr.asdl.minder.activities.fragments

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.google.android.material.snackbar.Snackbar
import fr.asdl.minder.R
import fr.asdl.minder.activities.MainActivity
import fr.asdl.minder.note.*
import fr.asdl.minder.note.bindings.NotePartAdapter
import fr.asdl.minder.note.bindings.NotePartDecoration
import fr.asdl.minder.view.options.Color
import fr.asdl.minder.view.options.ColorPicker
import fr.asdl.minder.view.rounded.RoundedImageView
import fr.asdl.minder.view.sentient.SentientRecyclerView

class EditorFragment : MinderFragment<Note>(), View.OnClickListener {

    override val layoutId: Int = R.layout.note_editor
    override lateinit var notable: Note
    override var menuLayoutId: Int? = R.menu.editor_menu
    override val styleId: Int = R.style.EditorTheme
    private var focusedNote: NotePart? = null

    override fun onLayoutInflated(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (activity as MainActivity).setSupportActionBar(toolbar)
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val editorToolbar: LinearLayout = view.findViewById(R.id.editor_toolbar)
        LayoutInflater.from(editorToolbar.context).inflate(R.layout.editor_toolbar, editorToolbar)
        (editorToolbar.getChildAt(0) as ViewGroup).children.filterIsInstance<RoundedImageView>().forEach {
            it.setOnClickListener(this)
        }
        this.updateContextToolbarEnabled(editorToolbar)

        // We add the note contents
        (view.findViewById<EditText>(R.id.note_editor_title)).setText(notable.title)
        (view.findViewById<EditText>(R.id.note_editor_title)).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                notable.title = s.toString()
                notable.save()
            }
        })

        val rec = (view.findViewById<SentientRecyclerView>(R.id.note_editor_elements))
        rec.visibility = View.VISIBLE
        rec.addItemDecoration(NotePartDecoration())
        val adapter = NotePartEditorAdapter(notable)
        rec.adapter = adapter
        rec.viewTreeObserver.addOnGlobalFocusChangeListener { _, newFocus ->
            this.focusedNote = if (newFocus != null) adapter.getHeldItem(rec.findContainingViewHolder(newFocus)?.adapterPosition ?: -1) else null
            this.updateContextToolbarEnabled(rec.rootView)
        }
        notable.notify = true
    }

    private fun updateContextToolbarEnabled(viewFrom: View) {
        viewFrom.findViewById<RoundedImageView>(R.id.moveIn)?.isEnabled =
            this.focusedNote != null && this.focusedNote?.hasAbove() == true
        viewFrom.findViewById<RoundedImageView>(R.id.moveOut)?.isEnabled =
            this.focusedNote != null && this.focusedNote?.getParentPart() != null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
            R.id.set_color -> {
                ColorPicker(activity!!, listOf(*Color.values()), Color.getIndex(notable.color), false) {
                    notable.color = it
                    this.updateBackgroundTint()
                    notable.save()
                }
            }
            else -> return false
        }
        return true
    }

    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {
            R.id.add_text_element -> notable.add(NoteText(""))
            R.id.add_checkbox_element -> notable.add(NoteCheckBoxable("", false))
            R.id.moveIn -> this.focusedNote?.moveIn()
            R.id.moveOut -> this.focusedNote?.moveOut()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notable.notify = false
    }

    override fun getTintBackgroundView(fragmentRoot: View): View? {
        return fragmentRoot.findViewById(R.id.transitionBackground)
    }

    private inner class NotePartEditorAdapter(note: Note) : NotePartAdapter<EditTextChangeWatcher>(note) {

        private var currentlyMoving: NotePart? = null
        private var currentlyMovingInitialOrder: Int? = null

        override fun onBindViewHolder(holder: ViewHolder<EditTextChangeWatcher>, content: NotePart) {
            super.onBindViewHolder(holder, content)
            if (content is TextNotePart) {
                val textView = (holder.findViewById(R.id.note_text) as? EditText)
                val watcher = EditTextChangeWatcher(content)
                holder.attach(watcher)
                textView?.addTextChangedListener(watcher)
                textView?.setText(content.content)
            }
            if (content is CheckableNotePart) {
                val checkBox = (holder.findViewById(R.id.note_checkbox) as? CheckBox)
                checkBox?.setOnClickListener { content.checked = checkBox.isChecked; notable.update(content, false) }
            }
        }

        override fun onViewRecycled(holder: ViewHolder<EditTextChangeWatcher>) {
            (holder.findViewById(R.id.note_text) as? EditText)?.removeTextChangedListener(holder.getAttachedData())
        }

        override fun getLayoutId(): Int {
            return R.layout.note_part_editor_layout
        }

        override fun onMoved(content: NotePart): Boolean = content.updateParentId()

        override fun onMoveChange(content: NotePart?) {
            this@EditorFragment.notable.expand(this.currentlyMoving, if (this.currentlyMoving != null)
                currentlyMoving!!.order - currentlyMovingInitialOrder!! else null)
            this.currentlyMoving = content
            this.currentlyMovingInitialOrder = content?.order
            this@EditorFragment.notable.collapse(this.currentlyMoving)
        }

        override fun onSwipeRight(context: Context, content: NotePart) {
            this.getDataHolder().remove(content)
            Snackbar.make(activity!!.findViewById(R.id.transitionContents), R.string.note_part_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.restore) {
                    this.getDataHolder().add(content)
                }.show()
        }

    }

    private inner class EditTextChangeWatcher(private val notePart: NotePart) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (notePart is TextNotePart && s != null) {
                (notePart as TextNotePart).content = s.toString()
                notable.update(notePart as NotePart, false)
            }
        }
    }

}