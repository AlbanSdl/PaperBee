package fr.asdl.paperbee.note.bindings

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import fr.asdl.paperbee.R
import fr.asdl.paperbee.note.NotePart
import fr.asdl.paperbee.view.sentient.SentientRecyclerViewAdapter

/**
 * Indents NoteParts depending on their depth
 */
class NotePartDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        with(outRect) {
            left = (((parent.adapter as? SentientRecyclerViewAdapter<*>)?.getHeldItem(parent.findContainingViewHolder(view)!!.adapterPosition) as? NotePart)?.getDepth() ?: 0) * parent.context.resources.getDimension(
                R.dimen.padding_small).toInt()
        }
    }

}