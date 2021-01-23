package fr.asdl.paperbee.storage

import android.text.Editable

interface SpanProcessor {

    fun serialize(editable: Editable): String

    fun deserialize(string: String): Editable

}