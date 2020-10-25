package fr.asdl.paperbee.view.sentient

/**
 * The kind of modification applied to a [DataHolderList]
 * [ADDITION] means a new item has been added to the set at the (changePosition) given position.
 * [REMOVAL] means an item has been removed from the set at the (changePosition) given position.
 * [CLEAR] means the set has been fully cleared.
 * [UPDATE] means an item has been updated in the set at the (changePosition) given position.
 * [MOVED] means an item has moved from a position (changePosition) to another one (otherPosition)
 * in the set.
 */
enum class ModificationType {
    ADDITION, REMOVAL, CLEAR, UPDATE, MOVED
}