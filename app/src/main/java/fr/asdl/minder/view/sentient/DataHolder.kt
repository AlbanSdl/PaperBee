package fr.asdl.minder.view.sentient

import java.util.*
import kotlin.collections.HashMap

abstract class DataHolderList<T: DataHolder> {
    private val listeners: HashMap<ModificationType, (Int, Int?) -> Unit> = HashMap()

    protected abstract fun retrieveContent(): LinkedList<T>
    protected abstract fun save(element: T)
    protected abstract fun delete(element: T)
    protected abstract fun shouldNotify(): Boolean

    fun getContents(): List<T> {
        return this.retrieveContent()
    }
    fun add(cnt: T, position: Int) {
        if (position >= this.retrieveContent().size) {
            this.retrieveContent().add(cnt)
            this.onChange(ModificationType.ADDITION, this.retrieveContent().size - 1, null)
        } else {
            this.retrieveContent().add(position, cnt)
            this.onChange(ModificationType.ADDITION, position, null)
        }
        this.save(cnt)
    }
    fun add(cnt: T) {
        this.retrieveContent().add(cnt)
        this.save(cnt)
        this.onChange(ModificationType.ADDITION, this.retrieveContent().size - 1, null)
    }
    fun update(position: Int, lambda: (old: T) -> T) {
        if (position < this.retrieveContent().size) {
            retrieveContent()[position] = lambda.invoke(retrieveContent()[position])
            this.save(retrieveContent()[position])
            this.onChange(ModificationType.UPDATE, position, null)
        }
    }
    fun update(cnt: T, executeListener: Boolean = true) {
        val index = this.retrieveContent().indexOf(cnt)
        if (index >= 0) {
            this.save(cnt)
            if (executeListener)
                this.onChange(ModificationType.UPDATE, index, null)
        } else {
            var realIndex: Int = -1
            for (elementIndex in 0 until this.retrieveContent().size) {
                if (this.getContents()[elementIndex].id == cnt.id) {
                    realIndex = elementIndex
                    break
                }
            }
            if (realIndex >= 0)
                this.update(realIndex) { cnt }
        }
    }
    fun remove(cnt: T?) {
        if (cnt == null) return
        val index = this.retrieveContent().indexOf(cnt)
        if (this.retrieveContent().remove(cnt) && index >= 0) {
            this.delete(cnt)
            this.onChange(ModificationType.REMOVAL, index, null)
        }
    }
    fun remove(index: Int) {
        if (this.retrieveContent().size > index && index >= 0) {
            this.delete(this.retrieveContent().removeAt(index))
            this.onChange(ModificationType.REMOVAL, index, null)
        }
    }
    fun move(fromPos: Int, toPos: Int) {
        if (fromPos < this.retrieveContent().size) {
            val realDestination = if (toPos < this.retrieveContent().size) toPos else (this.retrieveContent().size - 1)
            this.retrieveContent().add(realDestination, this.retrieveContent().removeAt(fromPos))
            this.onChange(ModificationType.MOVED, fromPos, realDestination)
        }
    }
    fun clear() {
        this.retrieveContent().forEach { this.delete(it) }
        this.retrieveContent().clear()
        this.onChange(ModificationType.CLEAR, 0, null)
    }

    private fun onChange(actionType: ModificationType, position: Int, toPosition: Int?) {
        if (shouldNotify()) this.listeners[actionType]?.invoke(position, toPosition)
    }

    fun on(actionType: ModificationType, lambda: (changePosition: Int, otherPosition: Int?) -> Unit) {
        this.listeners[actionType] = lambda
    }
}

interface DataHolder {
    val id: Int?
    val creationStamp: Long
}

enum class ModificationType {
    ADDITION, REMOVAL, CLEAR, UPDATE, MOVED
}