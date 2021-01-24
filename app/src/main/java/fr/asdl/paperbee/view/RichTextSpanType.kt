package fr.asdl.paperbee.view

import androidx.annotation.IdRes
import fr.asdl.paperbee.R

enum class RichTextSpanType(@IdRes val id: Int, val delimiter: String, val hasExtra: Boolean) {
    BOLD(R.id.bold, "b", false),
    ITALIC(R.id.italic, "i", false),
    UNDERLINE(R.id.underline, "u", false),
    COLOR(R.id.font_color, "t", true),
    BACKGROUND(R.id.background_color, "c", true),
    LINK(R.id.insert_link, "l", true);
}