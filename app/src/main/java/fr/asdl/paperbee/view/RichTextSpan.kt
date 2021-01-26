package fr.asdl.paperbee.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.text.style.*
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import fr.asdl.paperbee.view.options.Color

class RichTextSpan private constructor(val type: RichTextSpanType, val extra: Any?) {

    private var raw: CharacterStyle? = null

    private constructor(type: RichTextSpanType, extra: String?) : this(type, parseExtra(type, extra))

    constructor(@IdRes typeId: Int, extra: Any? = null) : this(findSpanType { it.id == typeId }
        ?: throw IllegalArgumentException(), extra)

    constructor(delimiter: String, extra: String? = null) : this(findSpanType { it.delimiter == delimiter }
        ?: throw IllegalArgumentException(), extra)

    internal constructor(characterStyle: CharacterStyle, context: Context) : this(
        getSpanType(characterStyle) ?: throw java.lang.IllegalArgumentException(),
        getSpanExtra(characterStyle, context)
    ) {
        this.raw = characterStyle
    }

    fun getExtraAsString(): String? {
        return when (this.extra) {
            is Color -> this.extra.tag
            is String -> this.extra
            else -> null
        }
    }

    fun getSpan(context: Context?): CharacterStyle? {
        if (raw == null && context != null) raw = when (type) {
            RichTextSpanType.BOLD -> StyleSpan(Typeface.BOLD)
            RichTextSpanType.ITALIC -> StyleSpan(Typeface.ITALIC)
            RichTextSpanType.UNDERLINE -> UnderlineSpan()
            RichTextSpanType.COLOR, RichTextSpanType.BACKGROUND -> {
                val cc = if (type == RichTextSpanType.COLOR) toggleContextDarkTheme(context) else context
                val color = ContextCompat.getColor(cc, (extra as Color).id)
                if (type == RichTextSpanType.COLOR) ForegroundColorSpan(color) else BackgroundColorSpan(
                    color
                )
            }
            RichTextSpanType.LINK -> {
                URLSpan(extra as String)
            }
        }
        return raw
    }

    companion object {

        private fun getSpanType(span: CharacterStyle): RichTextSpanType? {
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
                    val cc = if (characterStyle is ForegroundColorSpan) toggleContextDarkTheme(context) else context
                    for (i in Color.values())
                        if (ContextCompat.getColor(cc, i.id) == color) appColor = i
                    appColor
                }
                is URLSpan -> characterStyle.url
                else -> null
            }
        }

        private fun getInvertedTheme(context: Context): Int {
            return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> Configuration.UI_MODE_NIGHT_YES
                else -> Configuration.UI_MODE_NIGHT_NO
            }
        }

        private fun toggleContextDarkTheme(context: Context): Context {
            val conf = Configuration(context.resources.configuration)
            conf.uiMode =
                (getInvertedTheme(context) or (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()))
            return context.createConfigurationContext(conf)
        }
    }

}