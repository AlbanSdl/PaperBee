package fr.asdl.paperbee.exceptions

import java.lang.Exception

class IntAllocationException(int: Int) : Exception(
    "Cannot release non-allocated Integer $int"
)