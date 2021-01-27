package fr.asdl.paperbee.view.options

import androidx.annotation.ColorRes
import fr.asdl.paperbee.R

enum class FontColor(
    @ColorRes override val id: Int,
    val noteColorEquiv: NoteColor
) : IColor {
    ROSE(R.color.auxColorRose, NoteColor.ROSE),
    MAGENTA(R.color.auxColorMagenta, NoteColor.MAGENTA),
    BLUE(R.color.auxColorBlue, NoteColor.BLUE),
    CYAN(R.color.auxColorCyan, NoteColor.CYAN),
    GREEN(R.color.auxColorGreen, NoteColor.GREEN),
    YELLOW(R.color.auxColorYellow, NoteColor.YELLOW),
    ORANGE(R.color.auxColorOrange, NoteColor.ORANGE),
    RED(R.color.auxColorRed, NoteColor.RED);

    override val tag: String = noteColorEquiv.tag
    override val colorName: Int = noteColorEquiv.colorName

    companion object {
        internal fun get(selector: (FontColor) -> Boolean): FontColor? {
            for (i in values()) if (selector.invoke(i)) return i
            return null
        }
        fun fromTag(colorTag: String?): FontColor? {
            return get { it.noteColorEquiv.tag == colorTag }
        }
    }
}