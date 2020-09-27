package fr.asdl.minder.activities.fragments

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
import fr.asdl.minder.view.sentient.SentientRecyclerView

class EditorFragment(private val note: Note) : MinderFragment() {

    private val watchers = hashMapOf<NotePart, TextWatcher>()
    override val layoutId: Int = R.layout.note_editor
    override val menuLayoutId: Int = R.menu.editor_menu
    override val styleId: Int = R.style.EditorTheme

    override fun onLayoutInflated(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (activity as MainActivity).setSupportActionBar(toolbar)
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // We add the note contents
        (view.findViewById<EditText>(R.id.note_editor_title)).setText(note.title)
        (view.findViewById<EditText>(R.id.note_editor_title)).addTextChangedListener(EditTextChangeWatcher(note, null))

        val rec = (view.findViewById<SentientRecyclerView>(R.id.note_editor_elements))
        rec.visibility = View.VISIBLE
        rec.adapter = NotePartEditorAdapter(note)
        note.notify = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
            R.id.add_text_element -> note.add(NoteText("", parentId = note.id))
            R.id.add_checkbox_element -> note.add(NoteCheckBoxable("", false, parentId = note.id))
            else -> return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        note.notify = false
    }

    private inner class NotePartEditorAdapter(note: Note) : NoteAdapter.NotePartAdapter(note) {

        override fun onBindViewHolder(holder: ViewHolder, content: NotePart) {
            super.onBindViewHolder(holder, content)
            if (content is TextNotePart) {
                val textView = (holder.findViewById(R.id.note_text) as? EditText)
                this@EditorFragment.watchers[content] = EditTextChangeWatcher(note, content)
                textView?.addTextChangedListener(this@EditorFragment.watchers[content])
                textView?.setText(content.content)
            }
            if (content is CheckableNotePart) {
                val checkBox = (holder.findViewById(R.id.note_checkbox) as? CheckBox)
                checkBox?.setOnClickListener { content.checked = checkBox.isChecked }
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

}