package fr.asdl.paperbee.note.bindings

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import fr.asdl.paperbee.note.NoteText
import fr.asdl.paperbee.storage.v1.NotableContract
import fr.asdl.paperbee.view.RichTextEditable

class NotePartEditor(context: Context, attributeSet: AttributeSet): RichTextEditable<NoteText>(context, attributeSet) {

    override fun onTextUpdated(updated: Editable, attachedElement: NoteText) {
        attachedElement.apply {
            content = updated
            notifyDataChanged(NotableContract.NotableContractInfo.COLUMN_NAME_PAYLOAD)
            save(false)
        }
    }

}