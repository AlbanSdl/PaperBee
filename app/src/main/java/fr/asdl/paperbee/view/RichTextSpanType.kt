package fr.asdl.paperbee.view

import androidx.annotation.IdRes
import fr.asdl.paperbee.R

enum class RichTextSpanType(@IdRes val id: Int, val delimiter: String) {
    BOLD(R.id.bold, "b"),
    ITALIC(R.id.italic, "i"),
    UNDERLINE(R.id.underline, "u"),
    COLOR(-1, "t"),
    BACKGROUND(-1, "c"),
    LINK(-1, "l");
}