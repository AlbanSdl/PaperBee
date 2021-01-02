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
     * Retrieves the real order of the given element in the [DataHolderList]
     */
    private fun getRawOrder(element: T): Int = this.contents.indexOf(element)

    /**
     * Retrieves the order of the given element in the [DataHolderList]
     */
    fun getOrder(element: T): Int = this.getContents().indexOf(element)

    /**
     * Retrieves the raw position from the visible position (ie. given by an Adapter)
     */
    private fun getRawPosition(visiblePosition: Int): Int {
        return if (visiblePosition < this.getContents().size - 1 && visiblePosition >= 0)
            this.getRawOrder(this.getContents()[visiblePosition + 1]) - 1
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
        else
            this.reIndex(position)
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
            cnt.parentId = null
        }
        this.reIndex(cnt.order)
        cnt.order = -1
    }

    fun notifyUpdated(cnt: T) {
        this.onChange(ModificationType.UPDATE, getContents().indexOf(cnt), null)
    }

    /**
     * Moved a [DataHolder] in the data set.
     * If [fromPos] is out of bounds, nothing happens. If [toPos] is out of bounds, the item is
     * moved to the end of the set. The method re-indexes the elements of the data set.
     *
     * @param fromPos the initial index of the item.
     * @param toPos the index to move the item to.
     */
    fun move(fromPos: Int, toPos: Int) {
        if (fromPos < this.getContents().size && fromPos >= 0 && fromPos != toPos) {
            val realDestination = this.getRawPosition(toPos)
            val realFromPos = this.getRawPosition(fromPos)
            val elem = this.getContents()[fromPos]
            elem.order = realDestination
            if (realFromPos < realDestination)
                this.reIndex(realFromPos, realDestination - 1)
            else this.reIndex(realDestination + 1, realFromPos)
            this.onChange(ModificationType.MOVED, fromPos, toPos)
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
     * This method re-indexes [DataHolder] contained in the [DataHolderList] between to indexes
     * Move information on raw indexes: [contents], [getRawOrder]
     * @param fromPos the raw index to re-index from
     * @param toPos the raw index to re-index to (inclusive)
     */
    private fun reIndex(fromPos: Int, toPos: Int = this.contents.size - 1) {
        val max = this.contents.size - 1
        for (i in fromPos..toPos) {
            if (i > max) break
            this.contents[i].order = i
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