package fr.asdl.paperbee.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.text.style.*
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.content.ContextCompat
import fr.asdl.paperbee.view.options.Color

class RichTextSpan private constructor(val type: RichTextSpanType, private val extra: Any?) {

    private constructor(type: RichTextSpanType, extra: String?) : this(type, parseExtra(type, extra))

    constructor(@IdRes typeId: Int, extra: Any? = null) : this(findSpanType { it.id == typeId }
        ?: throw IllegalArgumentException(), extra)

    constructor(delimiter: String, extra: String? = null) : this(findSpanType { it.delimiter == delimiter }
        ?: throw IllegalArgumentException(), extra)

    constructor(characterStyle: CharacterStyle, context: Context) : this(
        getSpanType(characterStyle) ?: throw java.lang.IllegalArgumentException(),
        getSpanExtra(characterStyle, context)
    )

    fun getExtraAsString(): String? {
        return when (this.extra) {
            is Color -> this.extra.tag
            is String -> this.extra
            else -> null
        }
    }

    fun getSpan(context: Context): CharacterStyle {
        return when (type) {
            RichTextSpanType.BOLD -> StyleSpan(Typeface.BOLD)
            RichTextSpanType.ITALIC -> StyleSpan(Typeface.ITALIC)
            RichTextSpanType.UNDERLINE -> UnderlineSpan()
            RichTextSpanType.COLOR, RichTextSpanType.BACKGROUND -> {
                val cc = toggleContextDarkTheme(context)
                val color = ContextCompat.getColor(cc, (extra as Color).id)
                if (type == RichTextSpanType.COLOR) ForegroundColorSpan(color) else BackgroundColorSpan(
                    color
                )
            }
            RichTextSpanType.LINK -> {
                URLSpan(extra as String)
            }
        }
    }

    companion object {

        fun getSpanType(span: CharacterStyle): RichTextSpanType? {
            return when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        Typeface.BOLD -> RichTextSpanType.BOLD
                        Typeface.ITALIC -> RichTextSpanType.ITALIC
                        else -> null
                    }
                }
                is UnderlineSpan -> RichTextSpanType.UNDERLINE
                is ForegroundColorSpan -> RichTextSpanType.COLOR
                is BackgroundColorSpan -> RichTextSpanType.BACKGROUND
                is URLSpan -> RichTextSpanType.LINK
                else -> null
            }
        }

        private fun findSpanType(selector: (RichTextSpanType) -> Boolean): RichTextSpanType? {
            for (i in RichTextSpanType.values())
                if (selector.invoke(i)) return i
            return null
        }

        private fun parseExtra(type: RichTextSpanType, string: String?): Any? {
            return when (type) {
                RichTextSpanType.COLOR, RichTextSpanType.BACKGROUND -> Color.getFromTag(string)
                RichTextSpanType.LINK -> string
                else -> null
            }
        }

        private fun getSpanExtra(characterStyle: CharacterStyle, context: Context): Any? {
            return when (characterStyle) {
                is ForegroundColorSpan, is BackgroundColorSpan -> {
                    val color =
                        if (characterStyle is ForegroundColorSpan) characterStyle.foregroundColor
                        else (characterStyle as BackgroundColorSpan).backgroundColor
                    var appColor: Color? = null
                    for (i in Color.values())
                        if (ContextCompat.getColor(context, i.id) == color) appColor = i
                    appColor
                }
                is URLSpan -> characterStyle.url
                else -> null
            }
        }

        private fun getInvertedTheme(context: Context): Int {
            return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                MODE_NIGHT_NO -> MODE_NIGHT_YES
                else -> MODE_NIGHT_NO
            }
        }

        private fun toggleContextDarkTheme(context: Context): Context {
            val uiMode = getInvertedTheme(context) and Configuration.UI_MODE_NIGHT_MASK
            val conf = Configuration(context.resources.configuration)
            conf.uiMode =
                (uiMode or (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()))
            return context.createConfigurationContext(conf)
        }
    }

}