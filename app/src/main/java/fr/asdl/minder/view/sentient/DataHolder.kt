package fr.asdl.minder.view.sentient

import fr.asdl.minder.IntAllocator
import fr.asdl.minder.note.NoteManager
import kotlinx.serialization.Transient
import java.util.*
import kotlin.collections.HashMap

/**
 * This is the base class for every element that can be used in a [SentientRecyclerViewAdapter].
 * This structure handles the transmission of changes between the data set and the views.
 * This structure is relying on a [LinkedList] of any type inheriting from [DataHolder], which
 * gives ids to items manages their order.
 *
 * @param idAllocator the id allocator of the base NoteManager. It will be used to generate new ids
 * for any sub-component of its tree
 */
abstract class DataHolderList<T: DataHolder>(
    @Transient var idAllocator: IntAllocator?,
    @Transient override var noteManager: NoteManager?,
    override var parentId: Int?) : DataHolder {

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
     * an older one.
     * Only use this method in order to save the element as persistent data. If the element is part
     * of another one, don't do anything in this method.
     *
     * @param element the [DataHolder] to handle save for.
     */
    protected open fun save(element: T): Any = {}

    /**
     * Called when a element should be deleted. The item is asserted to exist and to have been
     * registered calling the [save] method.
     * Only use this method in order to delete the element as persistent data. If the element is
     * part of another one, don't do anything in this method.
     *
     * @param element the [DataHolder] to handle deletion for.
     * @param oldId the old id of the [element]
     */
    protected open fun delete(element: T, oldId: Int): Any = {}

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
    fun getContents(): List<T> = this.contents

    /**
     * Retrieves the parent of this [DataHolderList] by calling [findElementById] on the
     * [NoteManager] (the root of notes)
     */
    @Suppress("UNCHECKED_CAST")
    override fun getParent(): DataHolderList<DataHolderList<T>>? =
        noteManager?.findElementById(this.parentId) as? DataHolderList<DataHolderList<T>>

    /**
     * Retrieves any [DataHolder] that has the given [id], contained in any nested
     * [DataHolderList] contained in the current [DataHolderList].
     *
     * @param id the id of the element to look for.
     * @return the [DataHolder] if it has been found, null instead.
     */
    fun findElementById(id: Int?): DataHolder? {
        if (id == null) return null
        if (this.id == id) return this
        for (element in contents) {
            if (element.id == id) return element
            if (element is DataHolderList<*>) {
                val foundElement = element.findElementById(id)
                if (foundElement != null) return foundElement
            }
        }
        return null
    }

    /**
     * Saves the current [DataHolderList] until it reaches
     */
    fun save(shouldSaveRecursively: Boolean = true) {
        if (this.id == null) this.getParent()?.add(this) else this.getParent()?.update(this, false)
        if (shouldSaveRecursively)
            for (element in contents)
                this.saveRecursively(element, this)
        this.getParent()?.save(false)
    }

    /**
     * Allocates ids and saves data for every sub elements of the element to save.
     *
     * @param element the base [DataHolder] to save.
     * @param holderList the [DataHolderList] in which [element] is contained (or should be)
     */
    @Suppress("UNCHECKED_CAST")
    private fun <K: DataHolder> saveRecursively(element: K, holderList: DataHolderList<K>) {
        if (element.id == null)
            element.id = idAllocator?.allocate()
        else if (idAllocator?.isAllocated(element.id!!) == false) {
            val allocated = idAllocator?.forceAllocate(element.id!!)
            if (allocated != element.id!!) holderList.delete(element, element.id!!)
            element.id = allocated
        }
        element.parentId = holderList.id
        element.noteManager = noteManager
        if (element is DataHolderList<*>) {
            element.idAllocator = idAllocator
            element.getContents().forEach { saveRecursively(it, element as DataHolderList<DataHolder>) }
        }

        holderList.save(element)
    }

    /**
     * Allocates an id if the element hasn't one already and saves it.
     */
    private fun allocateAndSave(element: T) {
        this.saveRecursively(element, this)
    }

    /**
     * Releases the id of the element and deletes it from the current structure.
     */
    private fun releaseAndDelete(element: T) {
        if (element.id != null) {
            val oldId = element.id!!
            idAllocator?.release(element.id!!)
            element.id = null
            if (element is DataHolderList<*>)
                element.clear()
            this.delete(element, oldId)
        }
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
        this.allocateAndSave(cnt)
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
        this.allocateAndSave(cnt)
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
            this.releaseAndDelete(contents[position])
            contents[position] = lambda.invoke(contents[position])
            this.allocateAndSave(contents[position])
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
            this.allocateAndSave(cnt)
            if (executeListener)
                this.onChange(ModificationType.UPDATE, index, null)
        } else {
            var realIndex: Int = -1
            for (elementIndex in 0 until this.contents.size) {
                if (this.contents[elementIndex].id == cnt.id) {
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
            this.releaseAndDelete(cnt)
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
            this.reIndex(index + 1, indexDiff = -1)
            this.releaseAndDelete(this.contents.removeAt(index))
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
            this.allocateAndSave(elem)
            this.onChange(ModificationType.MOVED, fromPos, realDestination)
        }
    }

    /**
     * Clears the [DataHolderList] from all its content.
     * The deletion calls a single [ModificationType.CLEAR] rather than a [ModificationType.REMOVAL]
     * on every [DataHolder].
     */
    fun clear() {
        this.contents.forEach { this.releaseAndDelete(it) }
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
            this.allocateAndSave(this.contents[i])
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
    var id: Int?
    /**
     * The order of the [DataHolder] in its [DataHolderList]
     * Default (unset) value must be negative.
     */
    var order: Int
    /**
     * The id of the [DataHolderList] which contains this [DataHolder]
     * If this value is null, it means that this [DataHolder] has not been attached to any parent
     * or that it is contained in the note manager (in the root view)
     */
    var parentId: Int?
    /**
     * The note manager this [DataHolder] is registered in. Can be used to retrieve the parent
     */
    @Transient
    var noteManager: NoteManager?

    /**
     * Retrieves the [DataHolderList] which contains this [DataHolder].
     * This value can be null if the [DataHolder] has not been attached to any parent.
     */
    fun getParent(): DataHolderList<*>?
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