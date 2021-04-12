package fr.asdl.paperbee.note.bindings

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.IdRes
import fr.asdl.paperbee.note.NotePart
import fr.asdl.paperbee.note.TextNotePart
import fr.asdl.paperbee.storage.v1.NotableContract
import fr.asdl.paperbee.view.*

class NotePartEditor(context: Context, attributeSet: AttributeSet): RichTextEditable<TextNotePart>(context, attributeSet) {

    private var selectionListener: ((Boolean, NotePartEditor) -> Unit)? = null
    private var longClickListener: ((RichTextUrlSpan, NotePartEditor) -> Unit)? = null

    override fun onTextUpdated(updated: RichSpannable, attachedElement: TextNotePart) {
        if (attachedElement is NotePart) {
            attachedElement.apply {
                content = updated.toRawString()
                notifyDataChanged(NotableContract.NotableContractInfo.COLUMN_NAME_PAYLOAD)
                save(false)
            }
        }
    }

    override fun onSelectionUpdated(hasSelection: Boolean, selectionSpans: Array<RichTextSpan>) {
        this.selectionListener?.invoke(hasSelection, this)
    }

    override fun onUrlLongClick(span: RichTextUrlSpan) {
        this.longClickListener?.invoke(span, this)
    }

    fun setSelectionListener(listener: (Boolean, NotePartEditor) -> Unit) {
        this.selectionListener = listener
    }

    fun setUrlLongClickListener(listener: (RichTextUrlSpan, NotePartEditor) -> Unit) {
        this.longClickListener = listener
    }

    fun applyButtonSpan(@IdRes buttonId: Int) {
        this.applySpan(RichTextSpan(buttonId))
    }

    fun applyButtonSpanWithExtra(@IdRes buttonId: Int, extra: Any?) {
        this.applySpan(RichTextSpan(buttonId, extra))
    }

    fun getCurrentSelectionFullSpan(type: RichTextSpanType?): RichTextSpan? {
        if (type == null) return null
        return super.getSelectionSpans(type)
            .firstOrNull { super.getSpanStart(it) <= selectionStart && super.getSpanEnd(it) >= selectionEnd }
    }

}