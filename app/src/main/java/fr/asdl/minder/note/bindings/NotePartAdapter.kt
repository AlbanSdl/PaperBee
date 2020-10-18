package fr.asdl.minder.note.bindings

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import fr.asdl.minder.R
import fr.asdl.minder.note.*
import fr.asdl.minder.view.sentient.SentientRecyclerViewAdapter


abstract class NotePartAdapter(private val note: Note) : SentientRecyclerViewAdapter<NotePart>(note) {

    override fun onBindViewHolder(holder: ViewHolder, content: NotePart) {
        // TextNoteParts
        val textView = (holder.findViewById(R.id.note_text) as TextView)
        if (content is TextNotePart) {
            textView.text = content.content
            textView.visibility = View.VISIBLE
        }
        else textView.visibility = View.GONE
        // CheckableNotePart
        val checkBox = (holder.findViewById(R.id.note_checkbox) as CheckBox)
        if (content is CheckableNotePart) {
            checkBox.isChecked = content.checked
            checkBox.visibility = View.VISIBLE
            if (this.note.parentId != NoteManager.TRASH_ID)
                checkBox.setOnClickListener { content.checked = checkBox.isChecked; this.getDataHolder().update(content) }
            else
                checkBox.isEnabled = false
        } else {
            checkBox.visibility = View.GONE
            checkBox.setOnClickListener(null)
        }
    }

}