package fr.asdl.minder.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.asdl.minder.Fade
import fr.asdl.minder.R
import fr.asdl.minder.note.*
import fr.asdl.minder.view.sentient.SentientRecyclerView
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private var noteManager: NoteManager? = null
    private val statusCodeNoteSave = Random(Date().time).nextBits(16)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recycler = findViewById<SentientRecyclerView>(R.id.notes_recycler)
        Thread {
            noteManager = NoteManager(this)
            runOnUiThread {
                recycler.adapter = NoteAdapter(noteManager!!)
                Fade.fadeOut(findViewById(R.id.loadingBar))
                Fade.fadeOut(findViewById(R.id.loadingText))
                findViewById<FloatingActionButton>(R.id.add_note_button).setOnClickListener {
                    val note = Note("", noteManager = null)
                    note.add(NoteText(""))
                    this.openNote(note)
                }
            }
        }.start()
    }

    fun openNote(note: Note, vararg sharedViews: View) {
        startActivityForResult(
            Intent(this@MainActivity, NoteEditor::class.java).putExtra("note", Json.encodeToString(noteManager!!.serializer, note)),
            statusCodeNoteSave, ActivityOptionsCompat.makeSceneTransitionAnimation(this@MainActivity, *arrayOf(*sharedViews).plusElement(findViewById<FloatingActionButton>(R.id.add_note_button)).map { v -> Pair(v, ViewCompat.getTransitionName(v)) }.toTypedArray()).toBundle()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == statusCodeNoteSave && resultCode == Activity.RESULT_OK) {
            val noteStr = data?.getStringExtra("note")
            if (noteStr != null) {
                val note = Json.decodeFromString(noteManager!!.serializer, noteStr)
                note.noteManager = noteManager
                note.save()
            }
        }
    }
}