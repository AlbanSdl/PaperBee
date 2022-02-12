package fr.asdl.paperbee.view.sentient

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.asdl.paperbee.PaperBeeApplication
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

/**
 * An pre-implementation of the [RecyclerView.Adapter]. This handles the display of the contents of
 * the [RecyclerView] it is attached to. The data of the [SentientRecyclerViewAdapter] is provided
 * by a [DataHolderList] that notifies the [SentientRecyclerViewAdapter] every time a change
 * is made to the data set it contains.
 * The [SentientRecyclerViewAdapter] has been designed to work with the [SentientRecyclerView] but
 * should also work with a regular [RecyclerView].
 *
 * Already inflates the layout given in [getLayoutId] for any new [ViewHolder].
 * Customization can be made in [onBindViewHolder] to add the content of the [DataHolder].
 */
abstract class SentientRecyclerViewAdapter<T : DataHolder>(
    private val dataContainer: DataHolderList<T>
) : RecyclerView.Adapter<SentientRecyclerViewAdapter.ViewHolder>() {

    data class LinkedViewHolder(val holder: WeakReference<ViewHolder>, val position: Int)

    /**
     * Called on the initialization of the Adapter.
     * Initializes the communication with the DataHolderList.
     */
    init {
        this.dataContainer.on(ModificationType.ADDITION) { i: Int, i2: Int? -> if (i2 == null) super.notifyItemInserted(i) else super.notifyItemRangeInserted(i, i2) }
        this.dataContainer.on(ModificationType.REMOVAL) { i: Int, i2: Int? -> if (i2 == null) super.notifyItemRemoved(i) else super.notifyItemRangeRemoved(i, i2) }
        this.dataContainer.on(ModificationType.UPDATE) { i: Int, i2: Int? -> if (i2 == null) super.notifyItemChanged(i) else super.notifyItemRangeChanged(i, i2) }
        this.dataContainer.on(ModificationType.MOVED) { i: Int, it: Int? -> super.notifyItemMoved(i, it!!) }
        this.dataContainer.on(ModificationType.CLEAR) { _: Int, _: Int? -> super.notifyDataSetChanged() }
    }

    protected abstract val shouldDelayItems: Boolean
    private val loadingQueue: ArrayList<LinkedViewHolder> = arrayListOf()
    private var loaderProcessing = false
    private var loadingItems = if (this.dataContainer.size > 0) 1 else 0

    /**
     * Retrieves the [DataHolderList] containing the data set of the [SentientRecyclerViewAdapter].
     */
    fun getDataHolder(): DataHolderList<T> {
        return this.dataContainer
    }

    /**
     * Retrieves the [DataHolder] contained at the given position in the [DataHolderList].
     *
     * @param index the index of the [DataHolder] to retrieve.
     */
    fun getData(index: Int): T {
        return this.dataContainer.filtered[index]
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(this.getLayoutId(), parent, false))
    }

    final override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (this.shouldDelayItems) {
            this.loadingQueue.add(LinkedViewHolder(WeakReference(holder), position))
            if (!loaderProcessing) this.inflateNextViewHolder()
        } else {
            val item = this.getHeldItem(position)
            if (item != null) this.onBindViewHolder(holder, item)
        }
    }

    /**
     * Inflates the next [ViewHolder] of the [SentientRecyclerView]. This method will be used
     * when [shouldDelayItems] is set to true. This way, items are inflated asynchronously, one by
     * one. This must be used when all the items of the RecyclerView take too long to render.
     */
    private fun inflateNextViewHolder() {
        if (this.loadingQueue.isEmpty()) {
            this.loaderProcessing = false
            if (this.loadingItems != this.realItemCount) this.notifyItemInserted(this.loadingItems++)
            return
        }
        this.loaderProcessing = true
        val linkedData = this.loadingQueue.removeAt(0)
        val holder = this.getHeldItem(linkedData.position)
        val viewHolder = linkedData.holder.get()
        if (holder != null && viewHolder != null) (viewHolder.itemView.context.applicationContext as PaperBeeApplication).paperScope.launch {
            this@SentientRecyclerViewAdapter.onBindViewHolder(viewHolder, holder)
        }.invokeOnCompletion {
            if (it != null) throw it
            inflateNextViewHolder()
        } else this.loaderProcessing = false
    }

    /**
     * Customize the [ViewHolder] in this method. The [ViewHolder] has the layout given in
     * [getLayoutId]. However remember that this [ViewHolder] can already have been used. Thus don't
     * forget to reset values, visibility when you change them for some reason.
     *
     * @param holder the [ViewHolder] allocated to the [content]
     * @param content the [DataHolder] from the [DataHolderList] attached to the [ViewHolder]
     */
    abstract fun onBindViewHolder(holder: ViewHolder, content: T)

    final override fun getItemCount(): Int {
        return if (!this.shouldDelayItems || this.loadingItems >= this.realItemCount)
            this.realItemCount else this.loadingItems
    }

    private val realItemCount: Int
        get() = this.dataContainer.filtered.contents.size

    /**
     * Retrieves the id of the layout to use when new [ViewHolder] are inflated by the
     * [SentientRecyclerViewAdapter].
     *
     * @return the id of the layout to inflate.
     */
    abstract fun getLayoutId(): Int

    /**
     * Similar to [getData] at the difference that this method will never throw any
     * [ArrayIndexOutOfBoundsException] as the index is checked before accessing the data.
     *
     * @param pos the index of the [DataHolder] to retrieve.
     * @return the [DataHolder] contained at the given index if it exists. Returns null instead.
     */
    fun getHeldItem(pos: Int): T? {
        return if (this.itemCount > pos && pos >= 0) this.dataContainer.filtered[pos] else null
    }

    /**
     * Similar to a regular [RecyclerView.ViewHolder]. Only contains a shortened method
     * [findViewById] instead of [itemView].findViewById
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun findViewById(id: Int): View? {
            return this.itemView.findViewById(id)
        }

    }

    /**
     * Called when a the [ViewHolder] attached to the [content] has been swiped on the left.
     * This method will only be called if have set "app:allowSwipe" to "left" (or any composition
     * containing left)
     *
     * @param context the current android context
     * @param content the [DataHolder] attached to the swiped view
     */
    open fun onSwipeLeft(context: Context, content: T) {}

    /**
     * Called when a the [ViewHolder] attached to the [content] has been swiped on the right.
     * This method will only be called if have set "app:allowSwipe" to "right" (or any composition
     * containing right)
     *
     * @param context the current android context
     * @param content the [DataHolder] attached to the swiped view
     */
    open fun onSwipeRight(context: Context, content: T) {}

    /**
     * Called when a [ViewHolder] attached to the [content] has been moved in the [RecyclerView]
     * @param content the [DataHolder] corresponding to the moved view
     * @return whether the decorations should be invalidated
     */
    open fun onMoved(content: T): Boolean = false

    /**
     * Called when a [ViewHolder] attached to the [content] is being dragged or stops its movement.
     * @param content the element attached to the [ViewHolder]. Null if the element stops from being
     * dragged.
     */
    open fun onMoveChange(content: T?) {}
}