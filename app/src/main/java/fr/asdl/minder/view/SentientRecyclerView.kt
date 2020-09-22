package fr.asdl.minder.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.asdl.minder.Fade
import fr.asdl.minder.R
import fr.asdl.minder.note.SimpleSwipeBehaviour

class SentientRecyclerView(context: Context, attr: AttributeSet, defStyleAttr: Int) : RecyclerView(context, attr, defStyleAttr) {

    constructor(context: Context, attr: AttributeSet) : this(context, attr, 0)

    init {
        this.layoutManager = LinearLayoutManager(context)
        this.setHasFixedSize(false)
        ItemTouchHelper(SimpleSwipeBehaviour(this)).attachToRecyclerView(this)
    }

    private var emptyView: View? = null
    private val emptyViewRes: Int = attr.getAttributeResourceValue(context.getString(R.string.namespace),
        context.getString(R.string.namespaced_recycler_emptyViewId), -1)

    private val emptyObserver: AdapterDataObserver = object : AdapterDataObserver() {
        private fun update() {
            if (emptyView == null) {
                emptyView = (getContext() as Activity).findViewById(emptyViewRes)
                if (emptyView == null) return
            }
            if (adapter?.itemCount == 0)
                Fade.fadeIn(emptyView!!)
            else
                Fade.fadeOut(emptyView!!)
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

}