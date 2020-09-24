package fr.asdl.minder.note

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import fr.asdl.minder.R
import fr.asdl.minder.view.sentient.SentientRecyclerView
import fr.asdl.minder.view.sentient.SentientRecyclerViewAdapter

class NoteAdapter(dataContainer: NoteManager) : SentientRecyclerViewAdapter<Note>(dataContainer) {

    override fun getLayoutId(): Int {
        return R.layout.notes_layout
    }

    override fun onBindViewHolder(holder: ViewHolder, content: Note) {
        (holder.findViewById(R.id.note_title)!! as TextView).text = content.title
        val rec = (holder.findViewById(R.id.note_elements_recycler) as SentientRecyclerView)
        if (content.retrieveContent().size > 0) {
            rec.visibility = View.VISIBLE
            rec.adapter = NotePartAdapter(content, holder.findViewById(R.id.note_element) as View)
            rec.addTouchDelegation()
        } else {
            rec.visibility = View.GONE
        }
    }

    inner class NotePartAdapter(note: Note, private val clickDelegateView: View) : SentientRecyclerViewAdapter<NotePart>(note) {
        @SuppressLint("ClickableViewAccessibility")
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
                checkBox.setOnClickListener { content.checked = checkBox.isChecked; this.getDataHolder().update(content) }
            } else {
                checkBox.visibility = View.GONE
                checkBox.setOnClickListener(null)
            }
            holder.itemView.setOnTouchListener { v, e ->
                val rect = Rect()
                val rect2 = Rect()
                v.getGlobalVisibleRect(rect)
                clickDelegateView.getGlobalVisibleRect(rect2)
                clickDelegateView.onTouchEvent(MotionEvent.obtain(e.downTime, e.eventTime, e.action,
                    e.x + rect.left - rect2.left,
                    e.y + rect.top - rect2.top,
                    e.metaState))
                false
            }
            holder.itemView.setOnClickListener { clickDelegateView.performClick() }
        }

        override fun getLayoutId(): Int {
            return R.layout.note_part_layout
        }

    }

}