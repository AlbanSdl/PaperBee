package fr.asdl.minder.view.sentient

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.asdl.minder.Fade
import fr.asdl.minder.R

/**
 * A special [RecyclerView] that feels the presence of content in its data set.
 * It also handles swipe deletion gesture if the "app:allowSwipe" attribute is set to true.
 * You can choose only to swipe a part of the [RecyclerView.ViewHolder] by passing the id of the
 * view to swipe in the "app:swipeableView" attribute. Precise the "app:underSwipeView" if you
 * have problems with animations (if you put a view under the swipeableView and if you see it
 * because of animations such as "destroy" or "fade")
 * You can also provide a view that should be displayed if no element is contained in the Adapter
 * data set. You can do so by adding its id to the "app:emptyView" attribute.
 * The [SentientRecyclerView] also enforces the android:nestedScrollingEnabled="false" attribute
 * which doesn't work with multiple inputs at once (ie. when the user touches the screen with
 * multiple fingers)
 * The [SentientRecyclerView] has been designed to work with a [SentientRecyclerViewAdapter], using
 * a [DataHolderList] but this is NOT mandatory.
 */
class SentientRecyclerView(context: Context, attr: AttributeSet, defStyleAttr: Int) : RecyclerView(
    context,
    attr,
    defStyleAttr
) {

    /**
     * Used by android when inflating views from XML.
     */
    constructor(context: Context, attr: AttributeSet) : this(context, attr, 0)

    /**
     * Called on the initialization of the SentientRecyclerView. It attaches the
     * SentientSwipeBehaviour if requested in the AttributeSet.
     */
    init {
        if (this.layoutManager == null) this.layoutManager = SentientLinearLayoutManager(context)
        if (attr.getAttributeBooleanValue(
                context.getString(R.string.namespace),
                context.getString(R.string.namespaced_recycler_swipeable),
                true
            ))
            ItemTouchHelper(SentientSwipeBehaviour(this)).attachToRecyclerView(this)
    }

    /**
     * The view to display when the [RecyclerView.Adapter] contains no element.
     */
    private var emptyView: View? = null

    /**
     * Whether the [emptyView] was displayed.
     */
    private var wasEmptyDisplayed = false

    /**
     * The id of the [emptyView].
     */
    private val emptyViewRes: Int = attr.getAttributeResourceValue(
        context.getString(R.string.namespace),
        context.getString(R.string.namespaced_recycler_emptyViewId), -1
    )

    /**
     * The id of the Swipeable view as explained in the description of the [SentientRecyclerView].
     */
    val swipeableViewRes: Int = attr.getAttributeResourceValue(
        context.getString(R.string.namespace),
        context.getString(R.string.namespaced_recycler_swipeableViewId), -1
    )

    /**
     * The id of the view which is under the Swipeable view, as explained in the description of
     * [SentientRecyclerView].
     */
    val underSwipeableViewRes: Int = attr.getAttributeResourceValue(
        context.getString(R.string.namespace),
        context.getString(R.string.namespaced_recycler_underSwipeableViewId), -1
    )

    /**
     * An Observer that is notified every time the data set of the [RecyclerView.Adapter] changes.
     * This is used in order to detect when the [RecyclerView.Adapter] has no more items and to
     * display the [emptyView] at the proper time.
     */
    private val emptyObserver: AdapterDataObserver = object : AdapterDataObserver() {
        private fun setupEmptyView(): Boolean {
            if (emptyView == null) {
                emptyView = rootView.findViewById(emptyViewRes)
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

    /**
     * Sets the [RecyclerView.Adapter] of the [SentientRecyclerView].
     */
    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(emptyObserver)
        emptyObserver.onChanged()
    }

    /**
     * Adds touch delegation to the [SentientRecyclerView]. This means that the
     * [SentientRecyclerView] will not intercept touch events anymore. Thus the layouts and
     * views behind the [SentientRecyclerView] will get the [MotionEvent] too.
     */
    fun addTouchDelegation() {
        this.addOnItemTouchListener(ViewPropagationTouchListener())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        if (!this.isNestedScrollingEnabled) (this.layoutManager as? SentientLinearLayoutManager)?.setScrollEnabled(false)
        val returnValue = super.onTouchEvent(e)
        if (!this.isNestedScrollingEnabled) (this.layoutManager as? SentientLinearLayoutManager)?.setScrollEnabled(true)
        return returnValue
    }

    /**
     * A listener that tells the [SentientRecyclerView] not to intercept any [MotionEvent].
     * Used in the [addTouchDelegation] method.
     */
    private inner class ViewPropagationTouchListener : OnItemTouchListener {

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        }

    }

    /**
     * A custom [LinearLayoutManager] that allows the usage of fadingEdges and enforces
     * android:nestedScrollingEnabled="false"
     */
    private class SentientLinearLayoutManager(context: Context) : LinearLayoutManager(context) {

        private var isScrollEnabled: Boolean = true

        override fun canScrollHorizontally(): Boolean {
            return super.canScrollHorizontally() && isScrollEnabled
        }

        override fun canScrollVertically(): Boolean {
            return super.canScrollVertically() && isScrollEnabled
        }

        fun setScrollEnabled(enabled: Boolean) {
            this.isScrollEnabled = enabled
        }
    }

}