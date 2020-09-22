package fr.asdl.minder.note

import android.widget.TextView
import fr.asdl.minder.R
import fr.asdl.minder.view.SentientRecyclerViewAdapter

class NoteAdapter(dataContainer: NoteManager) : SentientRecyclerViewAdapter<Note>(dataContainer) {

    override fun getLayoutId(): Int {
        return R.layout.notes_layout
    }

    override fun onBindViewHolder(holder: ViewHolder, content: Note) {
        (holder.findViewById(R.id.note_title)!! as TextView).text = content.name
    }

}