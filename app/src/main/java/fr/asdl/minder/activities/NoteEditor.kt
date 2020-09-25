package fr.asdl.minder.activities

import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import fr.asdl.minder.R
import fr.asdl.minder.note.*
import fr.asdl.minder.preferences.SavedDataDirectory
import fr.asdl.minder.view.sentient.SentientRecyclerView
import kotlinx.serialization.json.Json

class NoteEditor : AppCompatActivity() {

    private var transitionContents: View? = null
    private var note: Note? = null
    private var dataDirectory: SavedDataDirectory? = null
    private val serializer = NoteSerializer()

    override fun onBackPressed() {
        this.animateFade(1f, 0f)
        supportFinishAfterTransition()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.dataDirectory = SavedDataDirectory(getString(R.string.notes_directory_name), this)
        if (intent.hasExtra("note")) note = Json.decodeFromString(
            NoteSerializer(), intent.getStringExtra(
                "note"
            )!!
        )

        setContentView(R.layout.note_editor)
        transitionContents = findViewById(R.id.transitionContents)!!
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // We add the note contents
        (findViewById<EditText>(R.id.note_editor_title)).setText(note?.title)
        (findViewById<EditText>(R.id.note_editor_title)).addTextChangedListener(EditTextChangeWatcher(note, null))

        val rec = (findViewById<SentientRecyclerView>(R.id.note_editor_elements))
        if (note != null && note!!.retrieveContent().size > 0) {
            rec.visibility = View.VISIBLE
            rec.adapter = NotePartEditorAdapter(note!!)
        } else {
            rec.visibility = View.GONE
        }

        // We perform the fade transition
        if (savedInstanceState == null)
            this.animateFade(0f, 1f, 250, 300)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun animateFade(from: Float, to: Float, delay: Long = 0, duration: Long = 200) {
        transitionContents!!.alpha = from
        val anim = ObjectAnimator.ofFloat(transitionContents!!, "alpha", from, to)
        anim.startDelay = delay
        anim.setDuration(duration).start()
    }

    private fun save() {
        if (this.dataDirectory != null && this.note != null)
            this.dataDirectory!!.saveDataAsync(note, serializer = serializer)
    }

    private inner class NotePartEditorAdapter(note: Note) : NoteAdapter.NotePartAdapter(note) {

        override fun onBindViewHolder(holder: ViewHolder, content: NotePart) {
            super.onBindViewHolder(holder, content)
            if (content is TextNotePart) {
                val textView = (holder.findViewById(R.id.note_text) as? EditText)
                textView?.addTextChangedListener(EditTextChangeWatcher(note, content))
            }
            if (content is CheckableNotePart) {
                val checkBox = (holder.findViewById(R.id.note_checkbox) as? CheckBox)
                checkBox?.setOnClickListener { content.checked = checkBox.isChecked }
            }
        }
        override fun getLayoutId(): Int {
            return R.layout.note_part_editor_layout
        }

    }

    override fun onPause() {
        this.save()
        super.onPause()
    }

    private class EditTextChangeWatcher(private val note: Note?, private val notePart: TextNotePart?) : TextWatcher {
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