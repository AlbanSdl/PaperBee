package fr.asdl.paperbee.view.sentient

import java.lang.IndexOutOfBoundsException
import java.util.*

/**
 * This is the base class for every element that can be used in a [SentientRecyclerViewAdapter].
 * This structure handles the transmission of changes between the data set and the views.
 * This structure is relying on a [LinkedList] of any type inheriting from [DataHolder], which
 * gives ids to items manages their order.
 *
 * Every change made to the data set is saved persistently.
 */
abstract class DataHolderList<T: DataHolder> : DataHolder() {

    inner class FilteredDataHolderList {
        /**
         * Retrieves an [List] with all the visible [DataHolder]s contained in this [DataHolderList],
         * sorted by order.
         * Modifications committed to this list will not be propagated back to the DataHolderList.
         */
        val contents: List<T> get() = this@DataHolderList.contents.filter { it.id !in filters }

        /**
         * Retrieves the VISIBLE [DataHolder] contained in this [DataHolderList]
         * with the given [order]
         */
        operator fun get(order: Int): T = this.contents[order]
        fun indexOf(element: T): Int = this.contents.indexOf(element)
    }

    /**
     * The listeners of the [DataHolder]. Registered by the [SentientRecyclerViewAdapter],
     * they are lambdas which will be called when their [ModificationType] is detected
     */
    private val listeners: EnumMap<ModificationType, (Int, Int?) -> Unit> = EnumMap(ModificationType::class.java)

    /**
     * The list of all the ids of hidden elements of the [DataHolderList]
     */
    private val filters = arrayListOf<Int>()

    /**
     * Retrieves the raw content of the data set.
     * It means that the [LinkedList] that stores data mush be returned AS IS (not filtered).
     * To get a pretty filtered list, use [filtered].
     */
    @Suppress("UNCHECKED_CAST")
    protected open val contents: LinkedList<T>
        get() = db?.findContent(this.id!!) as? LinkedList<T> ?: LinkedList()

    val size: Int get() = this.contents.size

    /**
     * Retrieves the [DataHolder] contained in this [DataHolderList] with the given [order]
     */
    operator fun get(order: Int): T = this.contents.find { it.order == order } ?: throw IndexOutOfBoundsException("No item for position $order")

    /**
     * Only retrieves the visible [DataHolder]s contained in this [DataHolderList].
     */
    val filtered: FilteredDataHolderList get() = FilteredDataHolderList()

    /**
     * Adds a [DataHolder] in the list at the given position. It will be inserted without
     * any deletion.
     *
     * @param cnt the [DataHolder] to insert in the set
     * @param position the raw index to insert the [cnt] to.
     */
    fun add(cnt: T, position: Int) {
        cnt.parentId = this.id!!
        if (position >= this.size || position < 0) {
            cnt.order = this.size
            cnt.db = this.db!!
            this.reIndex(cnt, false)
        } else {
            cnt.order = position
            cnt.db = this.db!!
            this.reIndex(cnt)
        }
        this.onChange(ModificationType.ADDITION, this.filtered.indexOf(cnt), null)
    }

    /**
     * Adds a [DataHolder] in the list. With this method, the [DataHolder] will be appended at
     * the end of the data set.
     *
     * @param cnt the [DataHolder] to append to the set
     */
    fun add(cnt: T) = this.add(cnt, cnt.order)

    /**
     * Removes a specific [DataHolder] from the data set.
     * If [cnt] is not in the set, nothing happens.
     *
     * @param cnt the [DataHolder] to removed from the set
     */
    open fun remove(cnt: T?) {
        if (cnt == null) return
        if (cnt.parentId == this.id!!) {
            val order = this.filtered.indexOf(cnt)
            cnt.parentId = null
            this.reIndex()
            this.onChange(ModificationType.REMOVAL, order, null)
        }
        cnt.order = -1
    }

    fun notifyUpdated(cnt: T) {
        this.onChange(ModificationType.UPDATE, filtered.indexOf(cnt), null)
    }

    /**
     * USES VISIBLE INDICES: Use this method from Adapters and other Objects that use such indices
     * Moved a [DataHolder] in the data set.
     * If [fromPos] is out of bounds, nothing happens. If [toPos] is out of bounds, the item is
     * moved to the end of the set. The method re-indexes the elements of the data set.
     *
     * @param fromPos the initial index of the item.
     * @param toPos the index to move the item to.
     */
    open fun move(fromPos: Int, toPos: Int) {
        val size = this.filtered.contents.size
        if (fromPos in 0 until size && toPos in 0 until size && fromPos != toPos)
            this.moveIndices(this.filtered[fromPos].order, this.filtered[toPos].order)
    }

    /**
     * Moves an element using indices (aka. order)
     */
    protected fun moveIndices(fromIndex: Int, toIndex: Int) {
        val size = this.size
        if (fromIndex in 0 until size && toIndex in 0 until size && fromIndex != toIndex) {
            val elem = this[fromIndex]
            val previousVisiblePosition = this.filtered.indexOf(elem)
            elem.order = toIndex
            this.reIndex(elem, fromIndex > toIndex)
            if (!this.filters.contains(elem.id))
                this.onChange(ModificationType.MOVED, previousVisiblePosition, this.filtered.indexOf(elem))
        }
    }

    /**
     * Clears the [DataHolderList] from all its content.
     * The deletion calls a single [ModificationType.CLEAR] rather than a [ModificationType.REMOVAL]
     * on every [DataHolder].
     */
    fun clear(deletion: Boolean = false) {
        this.contents.forEach { this.remove(it); if (deletion) this.db?.delete(it) }
        this.onChange(ModificationType.CLEAR, 0, null)
    }

    /**
     * Ensures the [DataHolderList] is sorted and all indices are filled.
     * @param element you may precise an element which will follow a specific rule. Use this
     * argument when there should be an order conflict
     * @param placeBefore the given [element] will be placed before the conflicted elements if this
     * property is true otherwise it will be placed after
     */
    protected fun reIndex(element: T? = null, placeBefore: Boolean = true) {
        val toIndex = this.contents.sortedWith { o1, o2 ->
            val placement = if (o1.order == o2.order) if (element == o1) -1 else if (element == o2) 1 else 0 else 0
            if (placement != 0) placement * if (placeBefore) 1 else -1 else o1.order - o2.order
        }
        for (i in toIndex.indices)
            toIndex[i].order = i
    }

    /**
     * This method is called every time an modification is made in the data set.
     *
     * @param actionType the type of modification
     * @param position the index of the beginning of the modification
     * @param toPosition the index of the end of the modification (included)
     */
    protected fun onChange(actionType: ModificationType, position: Int, toPosition: Int?) {
        this.listeners[actionType]?.invoke(position, toPosition)
    }

    /**
     * Sets the listener for the given [ModificationType].
     *
     * @param actionType the [ModificationType] to call the listener for
     * @param lambda the action to call when the [actionType] happens.
     * The changePosition and otherPosition arguments are inclusive.
     */
    fun on(actionType: ModificationType, lambda: (changePosition: Int, otherPosition: Int?) -> Unit) {
        this.listeners[actionType] = lambda
    }

    /**
     * Hides the chosen element from the current [DataHolderList]
     */
    fun hide(content: T) {
        if (content in this.filtered.contents) {
            this.onChange(ModificationType.REMOVAL, this.filtered.indexOf(content), null)
            this.filters.add(content.id!!)
        }
    }

    /**
     * Shows back the chosen element from the current [DataHolderList] after it has been hidden
     * with [hide]
     */
    fun show(content: T) {
        if (content.id!! in this.filters) {
            this.filters.remove(content.id!!)
            this.onChange(ModificationType.ADDITION, this.filtered.indexOf(content), null)
        }
    }
}