package fr.asdl.paperbee

import kotlin.math.max
import kotlin.math.min

/**
 * Represents an INCLUSIVE index/int range
 * @throws IllegalArgumentException if [start] is not strictly lower than [end]
 */
data class IndexRange(val start: Int, val end: Int) {

    init {
        if (length < 0) throw IllegalArgumentException("Bad range definition. Start and end are inverted.")
    }

    /**
     * Returns whether the range contains (this is not a strict comparison ie. the method
     * returns true if the ranges are the same)
     */
    fun contains(range: IndexRange): Boolean = this.start <= range.start && this.end >= range.end

    /**
     * Returns whether the current range can extend the given [range]
     */
    fun extends(range: IndexRange): Boolean {
        return this.and(range.shifted(-1, 1)).length > 0
    }

    /**
     * Returns the intersection of ranges.
     * If the ranges have no common value, a zero-length [IndexRange] will be returned.
     */
    fun and(range: IndexRange): IndexRange {
        if (this.end < range.start || range.end < this.start) return IndexRange(this.start, this.start)
        return IndexRange(max(range.start, this.start), min(range.end, this.end))
    }

    /**
     * If the given [range] can extend (see [extends] for more detail) the current [IndexRange]
     * this function will return the extended range which corresponds to the reunion of the two
     * ranges.
     */
    fun or(range: IndexRange): IndexRange? {
        if (!this.extends(range)) return null
        return IndexRange(min(range.start, this.start), max(range.end, this.end))
    }

    /**
     * Returns a copy of the current range, shifted at the beginning and at its end.
     */
    fun shifted(start: Int, end: Int): IndexRange {
        return IndexRange(this.start + start, this.end + end)
    }

    val length get() = end - start

    companion object {
        /**
         * Returns a range containing the maximum amount of values contained in the given
         * [ranges] without skipping any value.
         * Example: with the three following ranges [2,3] [4,5,6] and [8,9,10] this function
         * will return [2,3,4,5,6].
         * @throws IllegalArgumentException if no IndexRange is provided
         */
        fun merge(vararg ranges: IndexRange): IndexRange {
            val iRanges = arrayListOf<IndexRange>()
            for (range in ranges) {
                if (iRanges.isEmpty())
                    iRanges.add(range)
                else {
                    val comb = arrayListOf<IndexRange>()
                    val initial = arrayListOf<IndexRange>()
                    for (iRange in iRanges) {
                        val c = iRange.or(range)
                        if (c != null) {
                            comb.add(c)
                            initial.add(iRange)
                        }
                    }
                    if (initial.isNotEmpty() && comb.isNotEmpty()) {
                        iRanges.removeAll(initial)
                        iRanges.addAll(comb)
                    }
                }
            }
            return iRanges.maxByOrNull { it.length } ?: throw IllegalArgumentException()
        }
    }
}