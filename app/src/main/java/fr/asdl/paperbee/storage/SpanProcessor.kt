package fr.asdl.paperbee.storage

import android.os.Build
import android.text.Editable
import android.text.Html
import android.text.SpannableStringBuilder

@Suppress("DEPRECATION")
interface SpanProcessor {

    fun serialize(editable: Editable): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.toHtml(editable, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
        } else {
            Html.toHtml(editable)
        }
    }

    fun deserialize(string: String): Editable {
        return SpannableStringBuilder(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(string, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(string)
        })
    }

}