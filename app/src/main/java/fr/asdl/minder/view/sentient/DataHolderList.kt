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
 * Every change made to the data set is saved persistently using the first non-false [save]
 * method of the current [DataHolderList] and the ones of its parents.
 *
 * @param idAllocator the id allocator of the base NoteManager. It will be used to generate new ids
 * for any sub-component of its tree
 * @param noteManager the [NoteManager] which is the root of this hierarchy
 * @param parentId the id of the parent of this element (a [DataHolderList] that contains this
 * [DataHolderList].
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
     * Only use this method in order to save the element as persistent data. If the element is
     * part of another one, don't override this method.
     *
     * @param element the [DataHolder] to handle save for.
     * @return whether the save has been performed. In this case, save will not be dispatched to
     * the parent of the element.
     */
    protected open fun save(element: T): Boolean = false

    /**
     * Called when a element should be deleted. The item is asserted to exist and to have been
     * registered calling the [save] method.
     * Only use this method in order to delete the element as persistent data. If the element is
     * part of another one, don't override this method.
     *
     * @param element the [DataHolder] to handle deletion for.
     * @param oldId the old id of the [element]. In this case, deletion will not be performed to the
     * parent of the element.
     * @return whether the deletion has been performed. In this case, deletion will not be
     * dispatched to the parent of the element.
     */
    protected open fun delete(element: T, oldId: Int): Boolean = false

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
     * Saves the current [DataHolderList] and its hierarchy until in reaches a [save] method
     * returning true
     */
    fun save() {
        if ((if (this.id == null) this.getParent()?.add(this) else this.getParent()?.update(this, this.shouldNotify())) == false)
            this.getParent()!!.save()
    }

    /**
     * Allocates ids and saves data for every sub elements of the element to save.
     *
     * @param element the base [DataHolder] to save.
     * @param holderList the [DataHolderList] in which [element] is contained (or should be)
     */
    @Suppress("UNCHECKED_CAST")
    private fun <K: DataHolder> saveRecursively(element: K, holderList: DataHolderList<K>): Boolean {
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

        return holderList.save(element)
    }

    /**
     * Allocates an id if the element hasn't one already and saves it.
     *
     * @param element the element to save
     * @return whether data has been saved persistently
     */
    private fun allocateAndSave(element: T): Boolean {
        if (!this.saveRecursively(element, this))
            this.save()
        return true
    }

    /**
     * Releases the id of the element and deletes it from the current structure.
     *
     * @param element the element to delete
     * @return whether data has been persistently saved
     */
    private fun releaseAndDelete(element: T): Boolean {
        if (element.id != null) {
            val oldId = element.id!!
            idAllocator?.release(element.id!!)
            element.id = null
            if (element is DataHolderList<*>)
                element.clear()
            return this.delete(element, oldId)
        }
        return false
    }

    /**
     * Adds a [DataHolder] in the list at the given position. It will be inserted without
     * any deletion.
     *
     * @param cnt the [DataHolder] to insert in the set
     * @param position the index (0 based) to insert the [cnt] to.
     * @return whether data has been persistently saved
     */
    private fun add(cnt: T, position: Int): Boolean {
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
        return this.allocateAndSave(cnt)
    }

    /**
     * Adds a [DataHolder] in the list. With this method, the [DataHolder] will be appended at
     * the end of the data set.
     *
     * @param cnt the [DataHolder] to append to the set
     * @return whether data has been persistently saved
     */
    fun add(cnt: T): Boolean {
        if (cnt.order >= 0) return this.add(cnt, cnt.order)
        cnt.order = this.contents.size
        this.contents.add(cnt)
        val returnValue = this.allocateAndSave(cnt)
        this.onChange(ModificationType.ADDITION, this.contents.size - 1, null)
        return returnValue
    }

    /**
     * Updates a [DataHolder] contained in the data set.
     * The update will only be performed if the [position] is not out of bounds of the set.
     *
     * @param position the index of the [DataHolder] in the set
     * @param lambda use this lambda to apply changes to the [DataHolder]
     * @return whether data has been persistently saved
     */
    private fun update(position: Int, lambda: (old: T) -> T): Boolean {
        if (position < this.contents.size) {
            var returnValue = this.releaseAndDelete(contents[position])
            contents[position] = lambda.invoke(contents[position])
             returnValue = returnValue && this.allocateAndSave(contents[position])
            this.onChange(ModificationType.UPDATE, position, null)
            return returnValue
        }
        return false
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
     * @return whether data has been persistently saved
     */
    fun update(cnt: T, executeListener: Boolean = true): Boolean {
        val index = this.contents.indexOf(cnt)
        if (index >= 0) {
            val r = this.allocateAndSave(cnt)
            if (executeListener)
                this.onChange(ModificationType.UPDATE, index, null)
            return r
        } else {
            var realIndex: Int = -1
            for (elementIndex in 0 until this.contents.size) {
                if (this.contents[elementIndex].id == cnt.id) {
                    realIndex = elementIndex
                    break
                }
            }
            if (realIndex >= 0)
                return this.update(realIndex) { cnt }
        }
        return false
    }

    /**
     * Removes a specific [DataHolder] from the data set.
     * If [cnt] is not in the set, nothing happens.
     *
     * @param cnt the [DataHolder] to removed from the set.
     * @return whether data has been persistently saved
     */
    fun remove(cnt: T?): Boolean {
        if (cnt == null) return false
        val index = this.contents.indexOf(cnt)
        if (this.contents.remove(cnt) && index >= 0) {
            this.reIndex(index, indexDiff = -1)
            val r = this.releaseAndDelete(cnt)
            this.onChange(ModificationType.REMOVAL, index, null)
            return r
        }
        return false
    }

    /**
     * Removes the [DataHolder] from the set at the given position.
     * If position is out of bound, nothing happens.
     *
     * @param index the index (0 based) where to removed the [DataHolder] from the set.
     * @return whether data has been persistently saved
     */
    fun remove(index: Int): Boolean {
        if (this.contents.size > index && index >= 0) {
            this.reIndex(index + 1, indexDiff = -1)
            val r = this.releaseAndDelete(this.contents.removeAt(index))
            this.onChange(ModificationType.REMOVAL, index, null)
            return r
        }
        return false
    }

    /**
     * Moved a [DataHolder] in the data set.
     * If [fromPos] is out of bounds, nothing happens. If [toPos] is out of bounds, the item is
     * moved to the end of the set. The method re-indexes the elements of the data set.
     *
     * @param fromPos the initial index of the item.
     * @param toPos the index to move the item to.
     * @return whether data has been persistently saved
     */
    fun move(fromPos: Int, toPos: Int): Boolean {
        if (fromPos < this.contents.size && fromPos >= 0 && fromPos != toPos) {
            val realDestination = if (toPos < this.contents.size && toPos >= 0) toPos else (this.contents.size - 1)
            val elem = this.contents.removeAt(fromPos)
            elem.order = realDestination
            this.contents.add(realDestination, elem)
            if (fromPos < realDestination)
                this.reIndex(fromPos, realDestination - 1, -1)
            else this.reIndex(realDestination + 1, fromPos, 1)
            val r = this.allocateAndSave(elem)
            this.onChange(ModificationType.MOVED, fromPos, realDestination)
            return r
        }
        return false
    }

    /**
     * Clears the [DataHolderList] from all its content.
     * The deletion calls a single [ModificationType.CLEAR] rather than a [ModificationType.REMOVAL]
     * on every [DataHolder].
     *
     * @return whether data has been persistently saved
     */
    fun clear(): Boolean {
        var r = false
        this.contents.forEach { this.releaseAndDelete(it); r = true }
        this.contents.clear()
        this.onChange(ModificationType.CLEAR, 0, null)
        return r
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