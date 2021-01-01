package fr.asdl.paperbee.storage

import fr.asdl.paperbee.view.sentient.DataHolder

/**
 * Called as a callback after a SELECT SQL request. This is NOT called from UI thread !
 */
typealias SQLSelectionCallback = (dataHolders: List<DataHolder>) -> Unit

/**
 * Called as a callback after an INSERT SQL request. This is NOT called from UI thread !
 */
typealias SQLInsertionCallback = (inserted: Boolean) -> Unit

/**
 * Called as a callback after an UPDATE or DELETE SQL request. This is NOT called from UI thread !
 */
typealias SQLCountedCallback = (count: Int) -> Unit