package fr.asdl.minder.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.asdl.minder.Fade
import fr.asdl.minder.R
import fr.asdl.minder.note.Note
import fr.asdl.minder.note.NoteAdapter
import fr.asdl.minder.note.NoteManager
import fr.asdl.minder.note.NoteText
import fr.asdl.minder.view.SentientRecyclerView
import kotlinx.serialization.ExperimentalSerializationApi

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recycler = findViewById<SentientRecyclerView>(R.id.notes_recycler)
        Thread {
            val noteManager = NoteManager(this)
            runOnUiThread {
                recycler.adapter = NoteAdapter(noteManager)
                Fade.fadeOut(findViewById(R.id.loadingBar))
                Fade.fadeOut(findViewById(R.id.loadingText))
                findViewById<FloatingActionButton>(R.id.add_note_button).setOnClickListener {
                    val note = Note("Test Note... Click to add content", noteManager = noteManager)
                    noteManager.add(note)
                    note.add(NoteText("This is a test text", 0))
                }
            }
        }.start()
    }
}