package fr.asdl.minder.note

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import fr.asdl.minder.view.SentientRecyclerView
import fr.asdl.minder.view.SentientRecyclerViewAdapter

class SimpleSwipeBehaviour(private val sentientRecyclerView: SentientRecyclerView) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (sentientRecyclerView.adapter is SentientRecyclerViewAdapter<*>)
            (sentientRecyclerView.adapter as SentientRecyclerViewAdapter<*>).getDataHolder().remove(viewHolder.adapterPosition)
    }

}