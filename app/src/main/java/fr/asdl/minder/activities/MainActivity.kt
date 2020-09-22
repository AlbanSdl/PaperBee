package fr.asdl.minder.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.asdl.minder.Fade
import fr.asdl.minder.R
import fr.asdl.minder.note.Note
import fr.asdl.minder.note.NoteAdapter
import fr.asdl.minder.note.NoteManager
import fr.asdl.minder.view.SentientRecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recycler = findViewById<SentientRecyclerView>(R.id.notes_recycler)
        Thread {
            val noteManager = NoteManager(this)
            recycler.adapter = NoteAdapter(noteManager, findViewById(R.id.no_note))
            runOnUiThread {
                Fade.fadeIn(recycler.parent as View)
                Fade.fadeOut(findViewById(R.id.loadingBar))
                Fade.fadeOut(findViewById(R.id.loadingText))
                findViewById<FloatingActionButton>(R.id.add_note_button).setOnClickListener {
                    noteManager.add(Note(0, "Bonjour"))
                }
            }
        }.start()
    }
}