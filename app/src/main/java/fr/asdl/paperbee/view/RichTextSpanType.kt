package fr.asdl.paperbee.view

import android.graphics.Typeface
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.annotation.IdRes
import fr.asdl.paperbee.R
import java.lang.UnsupportedOperationException

enum class RichTextSpanType(private val span: CharacterStyle, @IdRes private val id: Int, val delimiter: String) {
    BOLD(StyleSpan(Typeface.BOLD), R.id.bold, "b"),
    ITALIC(StyleSpan(Typeface.ITALIC), R.id.italic, "i"),
    UNDERLINE(UnderlineSpan(), R.id.underline, "u");

    fun getSpan(): CharacterStyle {
        return when(span) {
            is StyleSpan -> StyleSpan(span.style)
            is UnderlineSpan -> UnderlineSpan()
            else -> throw UnsupportedOperationException("The span type `$name` has not been registered !")
        }
    }

    companion object {
        fun getSpanType(span: CharacterStyle): RichTextSpanType? {
            return when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        Typeface.BOLD -> BOLD
                        Typeface.ITALIC -> ITALIC
                        else -> null
                    }
                }
                is UnderlineSpan -> UNDERLINE
                else -> null
            }
        }
        private fun getSpanType(selector: (RichTextSpanType) -> Boolean): RichTextSpanType? {
            for (i in values())
                if (selector.invoke(i)) return i
            return null
        }
        fun getSpanType(@IdRes id: Int): RichTextSpanType? {
            return this.getSpanType { it.id == id }
        }
        fun getSpanType(delimiter: String): RichTextSpanType? {
            return this.getSpanType { it.delimiter == delimiter }
        }
    }
}