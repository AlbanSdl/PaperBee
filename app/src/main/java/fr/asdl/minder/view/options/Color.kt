package fr.asdl.minder.view.options

import androidx.annotation.ColorRes
import fr.asdl.minder.R

enum class Color(val tag: String, @ColorRes val id: Int) {
    RED("noteColorRed", R.color.noteColorRed),
    GREEN("noteColorGreen", R.color.noteColorGreen);

    companion object {
        fun getFromTag(colorTag: String): Color? {
            for (i in values()) if (i.tag == colorTag) return i
            return null
        }
    }
}