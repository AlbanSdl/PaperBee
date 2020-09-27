package fr.asdl.minder.activities.fragments

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.asdl.minder.R
import fr.asdl.minder.activities.MainActivity
import fr.asdl.minder.note.Note
import fr.asdl.minder.note.NoteAdapter
import fr.asdl.minder.note.NoteFolder
import fr.asdl.minder.note.NoteText
import fr.asdl.minder.view.sentient.SentientRecyclerView

class FolderFragment(private val folder: NoteFolder) : MinderFragment() {

    override val layoutId: Int = R.layout.folder_content

    override fun onLayoutInflated(view: View) {
        (activity as AppCompatActivity).setSupportActionBar(view.findViewById(R.id.folder_toolbar))
        (activity as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(folder.id != -1)
        view.findViewById<TextView>(R.id.folder_name).text = folder.title
        val recycler = view.findViewById<SentientRecyclerView>(R.id.notes_recycler)
        recycler.adapter = NoteAdapter(folder)
        view.findViewById<FloatingActionButton>(R.id.add_note_button).setOnClickListener {
            val note = Note("", folder.noteManager, idAllocator = folder.idAllocator, parentId = folder.id)
            folder.add(note)
            note.add(NoteText(""))
            (this.activity as MainActivity).openNotable(note)
        }
    }

}