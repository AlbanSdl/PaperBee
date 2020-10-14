package fr.asdl.minder.view.options

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import fr.asdl.minder.R

enum class Color(val tag: String, @ColorRes val id: Int, @StringRes val colorName: Int) {
    RED("noteColorRed", R.color.noteColorRed, R.string.colorRedName),
    GREEN("noteColorGreen", R.color.noteColorGreen, R.string.colorGreenName),
    BLUE("noteColorBlue", R.color.noteColorBlue, R.string.colorBlueName),
    YELLOW("noteColorYellow", R.color.noteColorYellow, R.string.colorYellowName),
    CYAN("noteColorCyan", R.color.noteColorCyan, R.string.colorCyanName),
    MAGENTA("noteColorMagenta", R.color.noteColorMagenta, R.string.colorMagentaName),
    ORANGE("noteColorOrange", R.color.noteColorOrange, R.string.colorOrangeName),
    ROSE("noteColorRose", R.color.noteColorRose, R.string.colorRoseName);

    companion object {
        fun getFromTag(colorTag: String): Color? {
            for (i in values()) if (i.tag == colorTag) return i
            return null
        }
        fun getIndex(color: Color?): Int? {
            val v = values()
            for (i in v.indices) if (v[i] == color) return i
            return null
        }
    }
}