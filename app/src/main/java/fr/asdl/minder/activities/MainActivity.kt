package fr.asdl.minder.activities

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


class MainActivity : AppCompatActivity() {

    private var noteManager: NoteManager? = null

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
                    val note = Note("Test Note !", noteManager = noteManager)
                    noteManager!!.add(note)
                    note.add(NoteText("This is a test text made to describe the note"))
                    note.add(NoteCheckBoxable("Does it really describe the note ?", false))
                    note.add(NoteCheckBoxable("Is the note working ? Check data persistence !", true))
                }
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        noteManager?.reload(this)
    }

    fun openNote(note: Note, vararg sharedViews: View) {
        startActivity(
            Intent(this@MainActivity, NoteEditor::class.java).putExtra("note", Json.encodeToString((note.noteManager as NoteManager).serializer, note)),
            ActivityOptionsCompat.makeSceneTransitionAnimation(this@MainActivity, *arrayOf(*sharedViews).map { v -> Pair(v, ViewCompat.getTransitionName(v)) }.toTypedArray()).toBundle()
        )
    }
}