package fr.asdl.minder.view.options

import androidx.annotation.ColorRes
import fr.asdl.minder.R

enum class Color(val tag: String, @ColorRes val id: Int) {
    RED("noteColorRed", R.color.noteColorRed),
    GREEN("noteColorGreen", R.color.noteColorGreen),
    BLUE("noteColorBlue", R.color.noteColorBlue),
    YELLOW("noteColorYellow", R.color.noteColorYellow),
    CYAN("noteColorCyan", R.color.noteColorCyan),
    MAGENTA("noteColorMagenta", R.color.noteColorMagenta);

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