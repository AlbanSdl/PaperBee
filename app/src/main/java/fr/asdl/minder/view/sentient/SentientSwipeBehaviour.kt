package fr.asdl.minder.view.sentient

import android.graphics.Canvas
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import fr.asdl.minder.R
import kotlin.math.max
import kotlin.math.min

/**
 * Handles Swipe gesture for [SentientRecyclerView]. Only works when the [SentientRecyclerView] it
 * is attached to uses a [SentientRecyclerViewAdapter] as [RecyclerView.Adapter]. (Otherwise
 * callback will not be called but the animation will be kept)
 *
 * The [SentientSwipeBehaviour] only tracks swipe to right gesture and attaches it to the deletion
 * of the swiped item.
 */
class SentientSwipeBehaviour(swipeDir: Int, private val sentientRecyclerView: SentientRecyclerView) :
    ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, swipeDir or ItemTouchHelper.UP or ItemTouchHelper.DOWN) {

    @Suppress("UNCHECKED_CAST")
    private fun asAdapter(action: (SentientRecyclerViewAdapter<DataHolder, *>) -> Unit) {
        val adapter = sentientRecyclerView.adapter as? SentientRecyclerViewAdapter<DataHolder, *>
        if (adapter != null) action.invoke(adapter)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        this.asAdapter { it.getDataHolder().move(viewHolder.adapterPosition, target.adapterPosition) }
        return true
    }

    override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                         fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
        this.asAdapter {
            if (it.onMoved(it.getData(viewHolder.adapterPosition)!!))
                recyclerView.invalidateItemDecorations()
        }
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        this.asAdapter {
            if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE)
                it.onMoveChange(if (viewHolder != null) it.getData(viewHolder.adapterPosition) else null)
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        this.asAdapter {
            val viewHolderAttached = it.getData(viewHolder.adapterPosition)
            if (direction == ItemTouchHelper.RIGHT && viewHolderAttached != null)
                it.onSwipeRight(viewHolder.itemView.context, viewHolderAttached)
            if (direction == ItemTouchHelper.LEFT && viewHolderAttached != null)
                it.onSwipeLeft(viewHolder.itemView.context, viewHolderAttached)
        }
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val view = this.getSwipeableView(recyclerView, viewHolder.itemView)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            val holderY = viewHolder.itemView.top - recyclerView.paddingTop
            val maxScrollY = recyclerView.height - recyclerView.paddingBottom - viewHolder.itemView.top - viewHolder.itemView.height
            super.onChildDraw(c, recyclerView, viewHolder, dX, min(max(dY, -holderY.toFloat()), maxScrollY.toFloat()), actionState, isCurrentlyActive)
        } else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val underViews = arrayListOf(this.getRightUnderSwipeableView(recyclerView, viewHolder.itemView), this.getLeftUnderSwipeableView(recyclerView, viewHolder.itemView))
            this.viewTranslate(view ?: viewHolder.itemView, dX, isCurrentlyActive, recyclerView, if (dX == 0f) null else underViews.removeAt(if (dX > 0) 0 else 1), *underViews.toTypedArray())
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        return when (val view = this.getSwipeableView(recyclerView, viewHolder.itemView)) {
            null -> super.clearView(recyclerView, viewHolder)
            else -> this.viewTranslate(view, 0f, false, recyclerView, null,
                this.getLeftUnderSwipeableView(recyclerView, viewHolder.itemView), this.getRightUnderSwipeableView(recyclerView, viewHolder.itemView))
        }
    }

    private fun viewTranslate(view: View, dX: Float, isCurrentlyActive: Boolean, recyclerView: RecyclerView,
                              underSwipeVisibleView: View?, vararg underSwipeClearingView: View?) {
        if (isCurrentlyActive && underSwipeVisibleView != null) {
            var originalElevation: Any? = view.getTag(R.id.item_touch_helper_previous_elevation)
            if (originalElevation == null) {
                originalElevation = ViewCompat.getElevation(view)
                val newElevation = 1f + findMaxElevation(recyclerView)
                ViewCompat.setElevation(view, newElevation)
                view.setTag(R.id.item_touch_helper_previous_elevation, originalElevation)
            }
        } else if (dX == 0f && underSwipeVisibleView == null) {
            val tag: Any? = view.getTag(R.id.item_touch_helper_previous_elevation)
            if (tag is Float)
                ViewCompat.setElevation(view, tag)
            view.setTag(R.id.item_touch_helper_previous_elevation, null)
        }
        underSwipeClearingView.forEach {
            if (it != null) it.visibility = View.GONE
        }
        underSwipeVisibleView?.visibility = View.VISIBLE
        view.translationX = dX
    }

    private fun getSwipeableView(recyclerView: RecyclerView, itemView: View): View? {
        return itemView.findViewById((recyclerView as? SentientRecyclerView)?.swipeableViewRes ?: -1)
    }

    private fun getLeftUnderSwipeableView(recyclerView: RecyclerView, itemView: View): View? {
        return itemView.findViewById((recyclerView as? SentientRecyclerView)?.leftUnderSwipeableViewRes ?: -1)
    }

    private fun getRightUnderSwipeableView(recyclerView: RecyclerView, itemView: View): View? {
        return itemView.findViewById((recyclerView as? SentientRecyclerView)?.rightUnderSwipeableViewRes ?: -1)
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