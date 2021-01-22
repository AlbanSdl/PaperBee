package fr.asdl.paperbee.note.bindings

import android.content.Context
import android.text.style.CharacterStyle
import android.util.AttributeSet
import fr.asdl.paperbee.note.NoteText
import fr.asdl.paperbee.storage.v1.NotableContract
import fr.asdl.paperbee.view.RichTextEditable

class NotePartEditor(context: Context, attributeSet: AttributeSet): RichTextEditable<NoteText>(context, attributeSet) {

    override fun onTextUpdated(newText: String, attachedElement: NoteText) {
        attachedElement.apply {
            content = newText
            notifyDataChanged(NotableContract.NotableContractInfo.COLUMN_NAME_PAYLOAD)
            save(false)
        }
    }

    override fun onSelectionUpdated(isEmpty: Boolean, style: Array<CharacterStyle>) {
    }

}