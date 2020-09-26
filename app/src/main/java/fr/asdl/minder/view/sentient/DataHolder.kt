package fr.asdl.minder.view.sentient

import java.util.*
import kotlin.collections.HashMap

/**
 * This is the base class for every element that can be used in a [SentientRecyclerViewAdapter].
 * This structure handles the transmission of changes between the data set and the views.
 * This structure is relying on a [LinkedList] of any type inheriting from [DataHolder], which
 * gives ids to items manages their order.
 */
abstract class DataHolderList<T: DataHolder> {

    /**
     * The listeners of the [DataHolder]. Registered by the [SentientRecyclerViewAdapter],
     * they are lambdas which will be called when their [ModificationType] is detected
     */
    private val listeners: HashMap<ModificationType, (Int, Int?) -> Unit> = HashMap()

    /**
     * Retrieves the raw content of the data set.
     * It means that the [LinkedList] that stores data mush be returned AS IS.
     * Indeed the returned [LinkedList] will be modified by the [DataHolderList]
     *
     * @return the RAW [LinkedList] where the [DataHolder] are actually stored.
     */
    protected abstract val contents: LinkedList<T>

    /**
     * Called when an element should be saved. It can be a brand new item in the set or
     * an older one. This function should also dedicate an id to the [DataHolder] if it hasn't any.
     *
     * @param element the [DataHolder] to handle save for.
     */
    protected abstract fun save(element: T)

    /**
     * Called when a element should be deleted. The item is asserted to exist and to have been
     * registered calling the [save] method. Thus this function should free the id of this
     * [DataHolder].
     *
     * @param element the [DataHolder] to handle deletion for.
     */
    protected abstract fun delete(element: T)

    /**
     * Details whether the listeners should be called after a modification in the data set.
     *
     * @return true if the listeners should be called, false instead.
     */
    protected abstract fun shouldNotify(): Boolean

    /**
     * Retrieves a [List] with all the [DataHolder]s contained in this [DataHolderList].
     *
     * @return the contents of the set
     */
    fun getContents(): List<T> {
        return this.contents
    }

    /**
     * Adds a [DataHolder] in the list at the given position. It will be inserted without
     * any deletion.
     *
     * @param cnt the [DataHolder] to insert in the set
     * @param position the index (0 based) to insert the [cnt] to.
     */
    fun add(cnt: T, position: Int) {
        if (position >= this.contents.size) {
            cnt.order = this.contents.size
            this.contents.add(cnt)
            this.onChange(ModificationType.ADDITION, this.contents.size - 1, null)
        } else {
            cnt.order = position
            this.reIndex(position, indexDiff = 1)
            this.contents.add(position, cnt)
            this.onChange(ModificationType.ADDITION, position, null)
        }
        this.save(cnt)
    }

    /**
     * Adds a [DataHolder] in the list. With this method, the [DataHolder] will be appended at
     * the end of the data set.
     *
     * @param cnt the [DataHolder] to append to the set
     */
    fun add(cnt: T) {
        if (cnt.order >= 0) return this.add(cnt, cnt.order)
        cnt.order = this.contents.size
        this.contents.add(cnt)
        this.save(cnt)
        this.onChange(ModificationType.ADDITION, this.contents.size - 1, null)
    }

    /**
     * Updates a [DataHolder] contained in the data set.
     * The update will only be performed if the [position] is not out of bounds of the set.
     *
     * @param position the index of the [DataHolder] in the set
     * @param lambda use this lambda to apply changes to the [DataHolder]
     */
    fun update(position: Int, lambda: (old: T) -> T) {
        if (position < this.contents.size) {
            contents[position] = lambda.invoke(contents[position])
            this.save(contents[position])
            this.onChange(ModificationType.UPDATE, position, null)
        }
    }

    /**
     * Updates a specific [DataHolder] contained in the set.
     * If [cnt] is contained as is in the set, it will save it and broadcast the changes to the
     * [SentientRecyclerViewAdapter]. Instead it will look for a [DataHolder] having the same id
     * in the data set and update it with the given [cnt] (saves it, and broadcasts the changes
     * as well). If no similar id is found, nothing happens and [cnt] is NOT appended in the set.
     *
     * @param cnt the [DataHolder] to update
     * @param executeListener whether a listener should be called. Only applies when if [cnt] is
     * already as is in the set.
     */
    fun update(cnt: T, executeListener: Boolean = true) {
        val index = this.contents.indexOf(cnt)
        if (index >= 0) {
            this.save(cnt)
            if (executeListener)
                this.onChange(ModificationType.UPDATE, index, null)
        } else {
            var realIndex: Int = -1
            for (elementIndex in 0 until this.contents.size) {
                if (this.getContents()[elementIndex].id == cnt.id) {
                    realIndex = elementIndex
                    break
                }
            }
            if (realIndex >= 0)
                this.update(realIndex) { cnt }
        }
    }

    /**
     * Removes a specific [DataHolder] from the data set.
     * If [cnt] is not in the set, nothing happens.
     *
     * @param cnt the [DataHolder] to removed from the set.
     */
    fun remove(cnt: T?) {
        if (cnt == null) return
        val index = this.contents.indexOf(cnt)
        if (this.contents.remove(cnt) && index >= 0) {
            this.reIndex(index, indexDiff = -1)
            this.delete(cnt)
            this.onChange(ModificationType.REMOVAL, index, null)
        }
    }

    /**
     * Removes the [DataHolder] from the set at the given position.
     * If position is out of bound, nothing happens.
     *
     * @param index the index (0 based) where to removed the [DataHolder] from the set.
     */
    fun remove(index: Int) {
        if (this.contents.size > index && index >= 0) {
            this.reIndex(index, indexDiff = -1)
            this.delete(this.contents.removeAt(index))
            this.onChange(ModificationType.REMOVAL, index, null)
        }
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
        if (fromPos < this.contents.size && fromPos >= 0 && fromPos != toPos) {
            val realDestination = if (toPos < this.contents.size && toPos >= 0) toPos else (this.contents.size - 1)
            val elem = this.contents.removeAt(fromPos)
            elem.order = realDestination
            this.contents.add(realDestination, elem)
            if (fromPos < realDestination)
                this.reIndex(fromPos, realDestination - 1, -1)
            else this.reIndex(realDestination + 1, fromPos, 1)
            this.save(elem)
            this.onChange(ModificationType.MOVED, fromPos, realDestination)
        }
    }

    /**
     * Clears the [DataHolderList] from all its content.
     * The deletion calls a single [ModificationType.CLEAR] rather than a [ModificationType.REMOVAL]
     * on every [DataHolder].
     */
    fun clear() {
        this.contents.forEach { this.delete(it) }
        this.contents.clear()
        this.onChange(ModificationType.CLEAR, 0, null)
    }

    /**
     * This method re-indexes [DataHolder] contained in the [DataHolderList] between to indexes
     *
     * @param fromPos the index to re-index from
     * @param toPos the index to re-index to (inclusive)
     * @param indexDiff position change (basically +1 or -1)
     */
    private fun reIndex(fromPos: Int, toPos: Int = this.contents.size - 1, indexDiff: Int) {
        val max = this.contents.size - 1
        for (i in fromPos..toPos) {
            if (i > max) break
            this.contents[i].order += indexDiff
            this.save(this.contents[i])
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
        if (shouldNotify()) this.listeners[actionType]?.invoke(position, toPosition)
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
}

/**
 * The base class for any item contained in a [DataHolderList]. This is useful for any element
 * to display in a [SentientRecyclerViewAdapter]
 */
interface DataHolder {
    /**
     * The id of the [DataHolder] (in his [DataHolderList])
     */
    val id: Int?
    /**
     * The order of the [DataHolder] in its [DataHolderList]
     * Default (unset) value must be negative.
     */
    var order: Int
}

/**
 * The kind of modification applied to a [DataHolderList]
 * [ADDITION] means a new item has been added to the set at the (changePosition) given position.
 * [REMOVAL] means an item has been removed from the set at the (changePosition) given position.
 * [CLEAR] means the set has been fully cleared.
 * [UPDATE] means an item has been updated in the set at the (changePosition) given position.
 * [MOVED] means an item has moved from a position (changePosition) to another one (otherPosition)
 * in the set.
 */
enum class ModificationType {
    ADDITION, REMOVAL, CLEAR, UPDATE, MOVED
}