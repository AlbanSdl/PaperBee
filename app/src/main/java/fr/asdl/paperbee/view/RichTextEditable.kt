package fr.asdl.paperbee.view

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.CharacterStyle
import android.util.AttributeSet
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatEditText
import fr.asdl.paperbee.IndexRange
import fr.asdl.paperbee.note.TextNotePart

abstract class RichTextEditable<T: TextNotePart>(context: Context, attributeSet: AttributeSet) :
    AppCompatEditText(context, attributeSet), TextWatcher {

    private var mAttachedElement: T? = null

    final override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        this.onSelectionUpdated(this.hasSelection(), this.getSelectionSpans())
    }

    protected abstract fun onSelectionUpdated(hasSelection: Boolean, selectionSpans: Array<CharacterStyle>)

    /**
     * Used to attach an object to the [RichTextEditable]. An eventual attached object
     * will always be detached before this operation, using [detach]
     */
    fun attach(attachedElement: T) {
        this.detach()
        mAttachedElement = attachedElement
        this.text = attachedElement.content
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

    /**
     * Toggles the chosen style to the selection. Can be used to remove the chosen style !
     * This method checks whether the selection is not empty using [hasSelection]
     * The new style is applied with the [Spannable.SPAN_EXCLUSIVE_INCLUSIVE] flag meaning any
     * inserted char right after the span will be included too.
     */
    protected fun applySpan(span: RichTextSpan) {
        if (this.hasSelection()) {
            val coverage = arrayListOf<IndexRange>()
            for (i in this.getSelectionSpans())
                if (RichTextSpan.getSpanType(i) === span.type) {
                    val startIndex = this.text!!.getSpanStart(i)
                    val endIndex = this.text!!.getSpanEnd(i)
                    this.text!!.apply {
                        removeSpan(i)
                        coverage.add(IndexRange(startIndex, endIndex))
                        if (selectionStart > startIndex)
                            setSpan(span.getSpan(context), startIndex, selectionStart, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        if (selectionEnd < endIndex)
                            setSpan(span.getSpan(context), selectionEnd, endIndex, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        if (IndexRange.merge(*coverage.toTypedArray()).contains(
                                IndexRange(selectionStart, selectionEnd)
                            )) {
                            this@RichTextEditable.onTextUpdated(this@RichTextEditable.text!!, mAttachedElement!!)
                            return@applySpan
                        }
                    }
                }
            this.text!!.setSpan(
                span.getSpan(context),
                this.selectionStart,
                this.selectionEnd,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            this.onTextUpdated(this.text!!, mAttachedElement!!)
        }
    }

    /**
     * This method is called on every update of the text.
     * May be used to notify and enqueue a save to a persistent form
     */
    protected abstract fun onTextUpdated(updated: Editable, attachedElement: T)

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    @CallSuper
    override fun afterTextChanged(s: Editable?) {
        if (mAttachedElement is T && s != null)
            this.onTextUpdated(s, mAttachedElement!!)
    }

}
