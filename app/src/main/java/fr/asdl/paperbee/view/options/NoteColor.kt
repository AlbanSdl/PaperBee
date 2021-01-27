package fr.asdl.paperbee.view.options

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import fr.asdl.paperbee.R

enum class NoteColor(
    override val tag: String,
    @ColorRes override val id: Int,
    @StringRes override val colorName: Int
) : IColor {
    ROSE("noteColorRose", R.color.noteColorRose, R.string.colorRoseName),
    MAGENTA("noteColorMagenta", R.color.noteColorMagenta, R.string.colorMagentaName),
    BLUE("noteColorBlue", R.color.noteColorBlue, R.string.colorBlueName),
    CYAN("noteColorCyan", R.color.noteColorCyan, R.string.colorCyanName),
    GREEN("noteColorGreen", R.color.noteColorGreen, R.string.colorGreenName),
    YELLOW("noteColorYellow", R.color.noteColorYellow, R.string.colorYellowName),
    ORANGE("noteColorOrange", R.color.noteColorOrange, R.string.colorOrangeName),
    RED("noteColorRed", R.color.noteColorRed, R.string.colorRedName);

    companion object {
        fun getFromTag(colorTag: String?): NoteColor? {
            for (i in values()) if (i.tag == colorTag) return i
            return null
        }
    }
}