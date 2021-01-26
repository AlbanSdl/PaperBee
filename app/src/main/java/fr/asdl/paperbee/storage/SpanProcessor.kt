package fr.asdl.paperbee.storage

import android.content.Context
import android.text.Editable
import fr.asdl.paperbee.view.RichSpannable

interface SpanProcessor {

    fun serialize(context: Context, editable: RichSpannable): String

    fun deserialize(context: Context, string: String): Editable

}