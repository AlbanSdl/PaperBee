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

    enum class LogicOperator {
        OR,
        AND
    }

    class FilterEntry(
        internal val column: String,
        internal val value: String,
        internal val operator: Operator = Operator.EQUALS
    )

    private var whereClause: String = ""
    private val args: ArrayList<String> = arrayListOf()

    /**
     * Adds multiple conditions with the same priority. In math it would be said all the
     * elements given below would be put in brackets. These conditions will be appended to the rest
     * of the filter with an AND operation.
     * @param link the operation to use between each entry, if different from AND
     */
    fun and(link: LogicOperator? = null, vararg filterEntries: FilterEntry): DatabaseFilter {
        this.append(LogicOperator.AND, link, *filterEntries)
        return this
    }

    /**
     * Adds multiple conditions with the same priority. In math it would be said all the
     * elements given below would be put in brackets. These conditions will be appended to the rest
     * of the filter with an OR operation.
     * @param link the operation to use between each entry, if different from OR
     */
    fun or(link: LogicOperator? = null, vararg filterEntries: FilterEntry): DatabaseFilter {
        this.append(LogicOperator.OR, link, *filterEntries)
        return this
    }

    private fun append(keyword: LogicOperator, link: LogicOperator? = null, vararg filterEntries: FilterEntry) {
        val usedLink = if (link == null && filterEntries.size > 1) keyword else link
        if (filterEntries.isNotEmpty()) {
            if (whereClause.isNotEmpty()) whereClause += " $keyword "
            if (filterEntries.size > 1) whereClause += "( "
            for (i in filterEntries.indices) {
                val entry = filterEntries[i]
                if (i != 0) whereClause += " $usedLink "
                whereClause += "${entry.column} ${entry.operator.symbol} ?"
                args.add(entry.value)
            }
            if (filterEntries.size > 1) whereClause += " )"
        }
    }

    internal fun getClause(): String = this.whereClause

    internal fun getArgs(): Array<String> = this.args.toTypedArray()

}