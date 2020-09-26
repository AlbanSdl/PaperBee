package fr.asdl.minder

import kotlinx.serialization.Serializable
import java.lang.Exception
import kotlin.collections.ArrayList

@Serializable
class IntAllocator(private val allocated: ArrayList<Int> = ArrayList()) {
    fun allocate(): Int {
        var i = 0
        while (i in allocated)
            i++
        allocated.add(i)
        return i
    }

    fun release(int: Int) {
        if (int !in allocated) throw Exception("Cannot release non-allocated Integer")
        this.allocated.remove(int)
    }

    fun forceAllocate(int: Int): Int {
        if (!allocated.contains(int)) {
            allocated.add(int)
            return int
        }
        return allocate()
    }

    private fun reset() {
        this.allocated.clear()
    }

    fun update(intAllocator: IntAllocator) {
        this.reset()
        this.allocated.addAll(intAllocator.allocated)
    }
}