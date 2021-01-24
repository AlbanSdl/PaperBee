package fr.asdl.paperbee.note.bindings

import android.content.Context
import android.text.Editable
import android.text.style.CharacterStyle
import android.util.AttributeSet
import androidx.annotation.IdRes
import fr.asdl.paperbee.note.NotePart
import fr.asdl.paperbee.note.TextNotePart
import fr.asdl.paperbee.storage.v1.NotableContract
import fr.asdl.paperbee.view.RichTextEditable
import fr.asdl.paperbee.view.RichTextSpan

class NotePartEditor(context: Context, attributeSet: AttributeSet): RichTextEditable<TextNotePart>(context, attributeSet) {

    private var selectionListener: ((Boolean, NotePartEditor) -> Unit)? = null

    override fun onTextUpdated(updated: Editable, attachedElement: TextNotePart) {
        if (attachedElement is NotePart) {
            attachedElement.apply {
                content = updated
                notifyDataChanged(NotableContract.NotableContractInfo.COLUMN_NAME_PAYLOAD)
                save(false)
            }
        }
    }

    override fun onSelectionUpdated(hasSelection: Boolean, selectionSpans: Array<CharacterStyle>) {
        this.selectionListener?.invoke(hasSelection, this)
    }

    fun setSelectionListener(listener: (Boolean, NotePartEditor) -> Unit) {
        this.selectionListener = listener
    }

    fun applyButtonSpan(context: Context, @IdRes buttonId: Int) {
        this.applySpan(context, RichTextSpan(buttonId))
    }

    fun applyButtonSpanWithExtra(context: Context, @IdRes buttonId: Int, extra: Any?) {
        this.applySpan(context, RichTextSpan(buttonId, extra))
    }

}