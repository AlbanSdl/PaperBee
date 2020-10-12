package fr.asdl.minder

import java.lang.Exception
import kotlin.collections.ArrayList

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

    fun isAllocated(int: Int): Boolean = int in allocated

    fun clear() = this.allocated.clear()
}