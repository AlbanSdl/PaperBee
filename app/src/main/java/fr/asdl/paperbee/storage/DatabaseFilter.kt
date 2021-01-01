package fr.asdl.paperbee.storage

class DatabaseFilter {

    @Suppress("unused")
    enum class Operator(val symbol: String) {
        EQUALS("="),
        NOT_EQUALS("<>"),
        LESS("<"),
        LESS_EQUALS("<="),
        GREATER(">"),
        GREATER_EQUALS(">="),
        LIKE("LIKE"),
        IN("IN")
    }

    class FilterEntry(
        internal val column: String,
        internal val value: String,
        internal val operator: Operator = Operator.EQUALS
    )

    private var whereClause: String = ""
    private val args: ArrayList<String> = arrayListOf()

    private fun assertSpacing() {
        if (whereClause.takeLast(1) != " ")
            whereClause += " "
    }

    /**
     * Adds multiple AND conditions with the same priority. In math it would be said all the
     * elements given below would be put in brackets.
     */
    fun and(vararg filterEntries: FilterEntry): DatabaseFilter {
        this.append("AND", *filterEntries)
        return this
    }

    /**
     * Adds multiple OR conditions with the same priority. In math it would be said all the
     * elements given below would be put in brackets.
     */
    fun or(vararg filterEntries: FilterEntry): DatabaseFilter {
        this.append("OR", *filterEntries)
        return this
    }

    private fun append(keyword: String, vararg filterEntries: FilterEntry) {
        if (filterEntries.isNotEmpty()) {
            assertSpacing()
            if (filterEntries.size > 1) whereClause += "( "
            for (i in filterEntries.indices) {
                val entry = filterEntries[i]
                if (i != 0) whereClause += " $keyword "
                whereClause += "${entry.column} ${entry.operator.symbol} ?"
                args.add(entry.value)
            }
            if (filterEntries.size > 1) whereClause += " )"
        }
    }

    internal fun getClause(): String = this.whereClause

    internal fun getArgs(): Array<String> = this.args.toTypedArray()

}