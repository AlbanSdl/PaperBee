package fr.asdl.paperbee.view

import android.graphics.Typeface
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.annotation.IdRes
import fr.asdl.paperbee.R
import java.lang.UnsupportedOperationException

enum class RichTextSpanType(private val span: CharacterStyle, @IdRes private val id: Int) {
    BOLD(StyleSpan(Typeface.BOLD), R.id.bold),
    ITALIC(StyleSpan(Typeface.ITALIC), R.id.italic),
    UNDERLINE(UnderlineSpan(), R.id.underline);

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
        fun getSpanType(@IdRes id: Int): RichTextSpanType? {
            for (i in values())
                if (i.id == id) return i
            return null
        }
    }
}