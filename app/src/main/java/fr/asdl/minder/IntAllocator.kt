package fr.asdl.minder

import java.lang.Exception
import kotlin.collections.ArrayList

class IntAllocator(private val allocated: ArrayList<Int> = ArrayList()) {
    fun allocate(): Int {
        var i = 0
        while (isAllocated(i))
            i++
        allocated.add(i)
        return i
    }

    private fun isAllocated(int: Int): Boolean {
        return int in allocated
    }

    fun release(int: Int) {
        if (!isAllocated(int)) throw Exception("Cannot release non-allocated Integer")
        this.allocated.remove(int)
    }

    fun forceAllocate(int: Int) {
        if (!allocated.contains(int)) allocated.add(int)
    }

    fun reset() {
        this.allocated.clear()
    }
}