package fr.asdl.paperbee.storage

import android.content.Context
import android.text.Editable

interface SpanProcessor {

    fun serialize(context: Context, editable: Editable): String

    fun deserialize(context: Context, string: String): Editable

}