package fr.asdl.paperbee.note.bindings

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import fr.asdl.paperbee.R
import fr.asdl.paperbee.note.Note
import fr.asdl.paperbee.note.NotePart


class NotePartAdapterStatic(note: Note, private val clickDelegateView: View? = null) : NotePartAdapter(note) {

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, content: NotePart) {
        super.onBindViewHolder(holder, content)
        if (clickDelegateView != null) {
            holder.itemView.setOnTouchListener { v, e ->
                val rect = Rect()
                val rect2 = Rect()
                v.getGlobalVisibleRect(rect)
                clickDelegateView.getGlobalVisibleRect(rect2)
                clickDelegateView.onTouchEvent(
                    MotionEvent.obtain(e.downTime, e.eventTime, e.action,
                    e.x + rect.left - rect2.left,
                    e.y + rect.top - rect2.top,
                    e.metaState))
                false
            }
            holder.itemView.isClickable = true
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.note_part_layout
    }

}