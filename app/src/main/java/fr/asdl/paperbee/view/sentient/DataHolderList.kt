package fr.asdl.paperbee.view.sentient

import java.util.*
import kotlin.collections.HashMap

/**
 * This is the base class for every element that can be used in a [SentientRecyclerViewAdapter].
 * This structure handles the transmission of changes between the data set and the views.
 * This structure is relying on a [LinkedList] of any type inheriting from [DataHolder], which
 * gives ids to items manages their order.
 *
 * Every change made to the data set is saved persistently using the first non-false [save]
 * method of the current [DataHolderList] and the ones of its parents.
 */
abstract class DataHolderList<T: DataHolder> : DataHolder() {

    /**
     * The listeners of the [DataHolder]. Registered by the [SentientRecyclerViewAdapter],
     * they are lambdas which will be called when their [ModificationType] is detected
     */
    private val listeners: HashMap<ModificationType, (Int, Int?) -> Unit> = HashMap()

    /**
     * The list of all the ids of hidden elements of the [DataHolderList]
     */
    private val filters = arrayListOf<Int>()

    /**
     * Retrieves the raw content of the data set.
     * It means that the [LinkedList] that stores data mush be returned AS IS.
     * Indeed the returned [LinkedList] will be modified by the [DataHolderList]
     *
     * @return the RAW [LinkedList] where the [DataHolder] are actually stored.
     */
    @Suppress("UNCHECKED_CAST")
    open val contents: LinkedList<T>
        get() = db?.findContent(this.id!!) as? LinkedList<T> ?: LinkedList()

    /**
     * Retrieves a [List] with all the visible [DataHolder]s contained in this [DataHolderList].
     *
     * @return the contents of the set
     */
    fun getContents(): List<T> = this.contents.filter { it.id !in this.filters }

    /**
     * Retrieves the order of the given element in the [DataHolderList]
     */
    private fun getOrder(element: T): Int = this.getContents().indexOf(element)

    /**
     * Retrieves the raw position from the visible position (ie. given by an Adapter)
     */
    private fun getRawPosition(visiblePosition: Int): Int {
        return if (visiblePosition in this.getContents().indices)
            this.contents.indexOf(this.getContents()[visiblePosition])
        else this.contents.size - 1
    }

    /**
     * Adds a [DataHolder] in the list at the given position. It will be inserted without
     * any deletion.
     *
     * @param cnt the [DataHolder] to insert in the set
     * @param position the raw index to insert the [cnt] to.
     */
    private fun add(cnt: T, position: Int) {
        cnt.parentId = this.id!!
        if (position >= this.contents.size || position < 0)
            cnt.order = this.contents.size
        else {
            this.updateIndex(1, position)
            cnt.order = position
        }
        cnt.db = this.db!!
        this.onChange(ModificationType.ADDITION, this.getOrder(cnt), null)
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
            val order = this.getOrder(cnt)
            cnt.parentId = null
            this.updateIndex(-1, cnt.order + 1)
            this.onChange(ModificationType.REMOVAL, order, null)
        }
        cnt.order = -1
    }

    fun notifyUpdated(cnt: T) {
        this.onChange(ModificationType.UPDATE, getContents().indexOf(cnt), null)
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
        val size = this.getContents().size
        if (fromPos in 0 until size && toPos in 0 until size && fromPos != toPos)
            this.moveIndices(this.getRawPosition(fromPos), this.getRawPosition(toPos))
    }

    /**
     * Moves an element using indices (aka. order)
     */
    protected fun moveIndices(fromIndex: Int, toIndex: Int) {
        val size = this.contents.size
        if (fromIndex in 0 until size && toIndex in 0 until size && fromIndex != toIndex) {
            val elem = this.contents[fromIndex]
            val previousVisiblePosition = this.getOrder(elem)
            if (fromIndex < toIndex) this.updateIndex(-1, fromIndex + 1, toIndex)
            else this.updateIndex(1, toIndex, fromIndex - 1)
            elem.order = toIndex
            if (!this.filters.contains(elem.id))
                this.onChange(ModificationType.MOVED, previousVisiblePosition, this.getOrder(elem))
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
     * Adds a number to the indices of all elements contained between index [fromIndex] and
     * [toIndex] (inclusive) in this DataHolderList. The indices you use as [fromIndex] and
     * [toIndex] are considered as the order of the elements.
     */
    private fun updateIndex(difference: Int, fromIndex: Int, toIndex: Int = this.contents.size - 1) {
        val maxIndex = this.contents.size - 1
        for (i in fromIndex..toIndex) {
            if (i > maxIndex) break
            this.contents[i].order += difference
        }
    }

    /**
     * This method is called every time an modification is made in the data set.
     *
     * @param actionType the type of modification
     * @param position the index of the beginning of the modification
     * @param toPosition the index of the end of the modification (included)
     */
    private fun onChange(actionType: ModificationType, position: Int, toPosition: Int?) {
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
        if (content in this.getContents()) {
            this.onChange(ModificationType.REMOVAL, this.getOrder(content), null)
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
            this.onChange(ModificationType.ADDITION, this.getOrder(content), null)
        }
    }
}