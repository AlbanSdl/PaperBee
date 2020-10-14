package fr.asdl.minder.activities.fragments

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import fr.asdl.minder.R
import fr.asdl.minder.activities.MainActivity
import fr.asdl.minder.note.*
import fr.asdl.minder.view.options.Color
import fr.asdl.minder.view.options.ColorPicker
import fr.asdl.minder.view.sentient.SentientRecyclerView

class EditorFragment : MinderFragment<Note>() {

    private val watchers = hashMapOf<NotePart, TextWatcher>()
    override val layoutId: Int = R.layout.note_editor
    override lateinit var notable: Note
    override var menuLayoutId: Int? = R.menu.editor_menu
    override val styleId: Int = R.style.EditorTheme

    override fun onLayoutInflated(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (activity as MainActivity).setSupportActionBar(toolbar)
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // We add the note contents
        (view.findViewById<EditText>(R.id.note_editor_title)).setText(notable.title)
        (view.findViewById<EditText>(R.id.note_editor_title)).addTextChangedListener(EditTextChangeWatcher(notable, null))

        val rec = (view.findViewById<SentientRecyclerView>(R.id.note_editor_elements))
        rec.visibility = View.VISIBLE
        rec.adapter = NotePartEditorAdapter(notable)
        notable.notify = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
            R.id.add_text_element -> notable.add(NoteText("", parentId = notable.id))
            R.id.add_checkbox_element -> notable.add(NoteCheckBoxable("", false, parentId = notable.id))
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

    override fun onDestroy() {
        super.onDestroy()
        notable.notify = false
    }

    private inner class NotePartEditorAdapter(note: Note) : NoteAdapter.NotePartAdapter(note) {

        override fun onBindViewHolder(holder: ViewHolder, content: NotePart) {
            super.onBindViewHolder(holder, content)
            if (content is TextNotePart) {
                val textView = (holder.findViewById(R.id.note_text) as? EditText)
                this@EditorFragment.watchers[content] = EditTextChangeWatcher(notable, content)
                textView?.addTextChangedListener(this@EditorFragment.watchers[content])
                textView?.setText(content.content)
            }
            if (content is CheckableNotePart) {
                val checkBox = (holder.findViewById(R.id.note_checkbox) as? CheckBox)
                checkBox?.setOnClickListener { content.checked = checkBox.isChecked; notable.update(content, false) }
            }
        }

        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            val textView = (holder.findViewById(R.id.note_text) as? EditText)
            if (textView != null)
                this@EditorFragment.watchers.values.forEach { textView.removeTextChangedListener(it) }
        }

        override fun getLayoutId(): Int {
            return R.layout.note_part_editor_layout
        }

        override fun onSwipeRight(context: Context, content: NotePart) {
            this.getDataHolder().remove(content)
        }

    }

    private class EditTextChangeWatcher(
        private val note: Note?,
        private val notePart: TextNotePart?
    ) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            if (note != null && notePart == null && s != null) note.title = s.toString()
            else if (note != null && notePart != null && s != null) {
                notePart.content = s.toString()
                note.update(notePart as NotePart, false)
            }
        }

    }

    override fun getTintBackgroundView(fragmentRoot: View): View? {
        return fragmentRoot.findViewById(R.id.transitionBackground)
    }

}