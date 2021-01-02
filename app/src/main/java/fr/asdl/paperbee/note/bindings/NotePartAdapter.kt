package fr.asdl.paperbee.note.bindings

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import fr.asdl.paperbee.R
import fr.asdl.paperbee.note.*
import fr.asdl.paperbee.storage.DatabaseProxy.Companion.TRASH_ID
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_EXTRA
import fr.asdl.paperbee.view.sentient.SentientRecyclerViewAdapter


abstract class NotePartAdapter<K>(private val note: Note) : SentientRecyclerViewAdapter<NotePart, K>(note) {

    override fun onBindViewHolder(holder: ViewHolder<K>, content: NotePart) {
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
            if (this.note.parentId != TRASH_ID)
                checkBox.setOnClickListener { content.checked = checkBox.isChecked; content.notifyDataChanged(COLUMN_NAME_EXTRA); content.save(false) }
            else
                checkBox.isEnabled = false
        } else {
            checkBox.visibility = View.GONE
            checkBox.setOnClickListener(null)
        }
    }

}