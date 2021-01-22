package fr.asdl.paperbee.view

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.CharacterStyle
import android.util.AttributeSet
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatEditText
import fr.asdl.paperbee.note.NotePart
import fr.asdl.paperbee.note.TextNotePart

abstract class RichTextEditable<T>(context: Context, attributeSet: AttributeSet) :
    AppCompatEditText(context, attributeSet), TextWatcher where T : NotePart, T : TextNotePart {

    private var mAttachedElement: T? = null

    /**
     * Used to attach an object to the [RichTextEditable]. An eventual attached object
     * will always be detached before this operation, using [detach]
     */
    fun attach(attachedElement: T) {
        this.detach()
        mAttachedElement = attachedElement
        this.setText(attachedElement.content)
        this.addTextChangedListener(this)
    }

    /**
     * Removes the current listener of the rich text editor.
     * This is may be seen as a replacement for RichTextEditable.attach(null)
     */
    fun detach() {
        this.removeTextChangedListener(this)
        this.mAttachedElement = null
    }

    private fun getSelectionSpans(): Array<CharacterStyle> {
        return this.text!!.getSpans(this.selectionStart, this.selectionEnd, CharacterStyle::class.java)
    }

    final override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        this.onSelectionUpdated(this.hasSelection(), this.getSelectionSpans())
    }

    /**
     * Applies the chosen style to the selection.
     * This method checks whether the selection is not empty using [hasSelection]
     * The new style is applied with the [Spannable.SPAN_EXCLUSIVE_INCLUSIVE] flag meaning any
     * inserted char right after the span will be included too.
     */
    fun applyStyle(spanType: CharacterStyle) {
        if (this.hasSelection()) {
            this.text!!.setSpan(
                spanType,
                this.selectionStart,
                this.selectionEnd,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
    }

    /**
     * This method is called on every update of the text.
     * May be used to notify and enqueue a save to a persistent form
     */
    protected abstract fun onTextUpdated(newText: String, attachedElement: T)

    /**
     * This method is called on every update of the user selection of the text.
     * Used to update the style editor
     */
    protected abstract fun onSelectionUpdated(isEmpty: Boolean, style: Array<CharacterStyle>)

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    @CallSuper
    override fun afterTextChanged(s: Editable?) {
        if (mAttachedElement is T && s != null)
            this.onTextUpdated(s.toString(), mAttachedElement!!)
    }

}