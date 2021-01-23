package fr.asdl.paperbee.storage

/**
 * Called as a callback after an INSERT SQL request. This is NOT called from UI thread !
 */
typealias SQLInsertionCallback = (inserted: Boolean) -> Unit

/**
 * Called as a callback after an UPDATE or DELETE SQL request. This is NOT called from UI thread !
 */
typealias SQLCountedCallback = (count: Int) -> Unit