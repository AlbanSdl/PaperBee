package fr.asdl.minder

import fr.asdl.minder.exceptions.IntAllocationException
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
        if (int !in allocated) throw IntAllocationException(int)
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