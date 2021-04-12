package fr.asdl.paperbee

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TestIndexRangeGroup {
    @Test
    fun test() {
        println(
            IndexRange.group(
                IndexRange(3, 10),
                IndexRange(7, 16),
                IndexRange(1, 2),
                IndexRange(15, 18),
                IndexRange(19, 27),
                IndexRange(30, 35),
                IndexRange(14, 15),
                IndexRange(8, 14)
            ).map { it.toString() }
        )
    }
}