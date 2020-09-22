package fr.asdl.minder.view

import java.util.*
import kotlin.collections.HashMap

abstract class DataHolderList<T: DataHolder> {
    private val listeners: HashMap<ModificationType, (Int, Int?) -> Unit> = HashMap()

    abstract fun retrieveContent(): LinkedList<T>
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
    }
    fun add(cnt: T) {
        this.retrieveContent().add(cnt)
        this.onChange(ModificationType.ADDITION, this.retrieveContent().size - 1, null)
    }
    fun update(position: Int, lambda: (old: T) -> T) {
        if (position < this.retrieveContent().size) {
            retrieveContent()[position] = lambda.run{retrieveContent()[position]}
            this.onChange(ModificationType.UPDATE, position, null)
        }
    }
    fun remove(cnt: T) {
        val index = this.retrieveContent().indexOf(cnt)
        if (this.retrieveContent().remove(cnt) && index >= 0)
            this.onChange(ModificationType.REMOVAL, index, null)
    }
    fun move(fromPos: Int, toPos: Int) {
        if (fromPos < this.retrieveContent().size) {
            val realDestination = if (toPos < this.retrieveContent().size) toPos else (this.retrieveContent().size - 1)
            this.retrieveContent().add(realDestination, this.retrieveContent().removeAt(fromPos))
            this.onChange(ModificationType.MOVED, fromPos, realDestination)
        }
    }
    fun clear() {
        this.retrieveContent().clear()
        this.onChange(ModificationType.CLEAR, 0, null)
    }

    private fun onChange(actionType: ModificationType, position: Int, toPosition: Int?) {
        this.listeners[actionType]?.invoke(position, toPosition)
    }

    fun on(actionType: ModificationType, lambda: (changePosition: Int, otherPosition: Int?) -> Unit) {
        this.listeners[actionType] = lambda
    }
}

interface DataHolder

enum class ModificationType {
    ADDITION, REMOVAL, CLEAR, UPDATE, MOVED
}