package fr.asdl.minder.view.sentient

import android.graphics.Canvas
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import fr.asdl.minder.R

class SentientSwipeBehaviour(private val sentientRecyclerView: SentientRecyclerView) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        (sentientRecyclerView.adapter as? SentientRecyclerViewAdapter<*>)?.getDataHolder()?.remove(viewHolder.adapterPosition)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        return when (val view = this.getSwipeableView(recyclerView, viewHolder.itemView)) {
            null -> super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            else -> this.viewTranslate(view, dX, isCurrentlyActive, recyclerView)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        return when (val view = this.getSwipeableView(recyclerView, viewHolder.itemView)) {
            null -> super.clearView(recyclerView, viewHolder)
            else -> this.viewTranslate(view, 0f, false, null)
        }
    }

    private fun viewTranslate(view: View, dX: Float, isCurrentlyActive: Boolean, recyclerView: RecyclerView?) {
        if (isCurrentlyActive && recyclerView != null) {
            var originalElevation: Any? = view.getTag(R.id.item_touch_helper_previous_elevation)
            if (originalElevation == null) {
                originalElevation = ViewCompat.getElevation(view)
                val newElevation = 1f + findMaxElevation(recyclerView)
                ViewCompat.setElevation(view, newElevation)
                view.setTag(R.id.item_touch_helper_previous_elevation, originalElevation)
            }
        } else if (dX == 0f && recyclerView == null) {
            val tag: Any? = view.getTag(R.id.item_touch_helper_previous_elevation)
            if (tag is Float)
                ViewCompat.setElevation(view, tag)
            view.setTag(R.id.item_touch_helper_previous_elevation, null)
        }
        view.translationX = dX
    }

    private fun getSwipeableView(recyclerView: RecyclerView, itemView: View): View? {
        return itemView.findViewById((recyclerView as? SentientRecyclerView)?.swipeableViewRes ?: -1)
    }

    private fun findMaxElevation(recyclerView: RecyclerView): Float {
        val childCount = recyclerView.childCount
        var max = 0f
        for (i in 0 until childCount) {
            val child = this.getSwipeableView(recyclerView, recyclerView.getChildAt(i)) ?: recyclerView.getChildAt(i)
            val elevation = ViewCompat.getElevation(child)
            if (elevation > max)
                max = elevation
        }
        return max
    }

}