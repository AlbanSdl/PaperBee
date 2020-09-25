package fr.asdl.minder.view.sentient

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.asdl.minder.Fade
import fr.asdl.minder.R

class SentientRecyclerView(context: Context, attr: AttributeSet, defStyleAttr: Int) : RecyclerView(
    context,
    attr,
    defStyleAttr
) {

    constructor(context: Context, attr: AttributeSet) : this(context, attr, 0)

    init {
        if (this.layoutManager == null) this.layoutManager = LinearLayoutManager(context)
        this.setHasFixedSize(false)
        if (attr.getAttributeBooleanValue(
                context.getString(R.string.namespace),
                context.getString(R.string.namespaced_recycler_swipeable),
                true
            ))
            ItemTouchHelper(SentientSwipeBehaviour(this)).attachToRecyclerView(this)
    }

    private var emptyView: View? = null
    private var wasEmptyDisplayed = false
    private val emptyViewRes: Int = attr.getAttributeResourceValue(
        context.getString(R.string.namespace),
        context.getString(R.string.namespaced_recycler_emptyViewId), -1
    )
    val swipeableViewRes: Int = attr.getAttributeResourceValue(
        context.getString(R.string.namespace),
        context.getString(R.string.namespaced_recycler_swipeableViewId), -1
    )

    private val emptyObserver: AdapterDataObserver = object : AdapterDataObserver() {
        private fun setupEmptyView(): Boolean {
            if (emptyView == null) {
                emptyView = (getContext() as Activity).findViewById(emptyViewRes)
                if (emptyView == null) return false
            }
            return true
        }
        private fun update() {
            if (wasEmptyDisplayed != (adapter?.itemCount == 0) && setupEmptyView()) {
                wasEmptyDisplayed = adapter?.itemCount == 0
                if (wasEmptyDisplayed)
                    Fade.fadeIn(emptyView!!)
                else
                    Fade.fadeOut(emptyView!!)
            }
        }
        override fun onChanged() {
            this.update()
        }
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            this.update()
        }
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            this.update()
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(emptyObserver)
        emptyObserver.onChanged()
    }

    fun addTouchDelegation() {
        this.addOnItemTouchListener(ViewPropagationTouchListener())
    }

    private inner class ViewPropagationTouchListener : OnItemTouchListener {

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        }

    }

}