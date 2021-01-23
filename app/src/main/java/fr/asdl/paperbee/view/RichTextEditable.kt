package fr.asdl.paperbee.view

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.CharacterStyle
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatEditText
import fr.asdl.paperbee.IndexRange
import fr.asdl.paperbee.R
import fr.asdl.paperbee.note.NotePart
import fr.asdl.paperbee.note.TextNotePart

abstract class RichTextEditable<T>(context: Context, attributeSet: AttributeSet) :
    AppCompatEditText(context, attributeSet), TextWatcher, ActionMode.Callback where T : NotePart, T : TextNotePart {

    private var mAttachedElement: T? = null

    /**
     * Used to attach an object to the [RichTextEditable]. An eventual attached object
     * will always be detached before this operation, using [detach]
     */
    fun attach(attachedElement: T) {
        this.detach()
        mAttachedElement = attachedElement
        this.text = attachedElement.content
        this.addTextChangedListener(this)
        this.customSelectionActionModeCallback = this
    }

    /**
     * Removes the current listener of the rich text editor.
     * This is may be seen as a replacement for RichTextEditable.attach(null)
     */
    fun detach() {
        this.removeTextChangedListener(this)
        this.mAttachedElement = null
        this.customSelectionActionModeCallback = null
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
    private fun applySpan(spanType: RichTextSpanType) {
        if (this.hasSelection()) {
            val coverage = arrayListOf<IndexRange>()
            for (i in this.getSelectionSpans())
                if (RichTextSpanType.getSpanType(i) === spanType) {
                    val startIndex = this.text!!.getSpanStart(i)
                    val endIndex = this.text!!.getSpanEnd(i)
                    this.text!!.apply {
                        removeSpan(i)
                        coverage.add(IndexRange(startIndex, endIndex))
                        if (selectionStart > startIndex)
                            setSpan(spanType.getSpan(), startIndex, selectionStart, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        if (selectionEnd < endIndex)
                            setSpan(spanType.getSpan(), selectionEnd, endIndex, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        if (IndexRange.merge(*coverage.toTypedArray()).contains(
                                IndexRange(selectionStart, selectionEnd)
                            ))
                            return@applySpan
                    }
                }
            this.text!!.setSpan(
                spanType.getSpan(),
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

    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        menu?.apply {
            removeItem(android.R.id.selectAll)
            removeItem(android.R.id.shareText)
        }
        mode?.menuInflater?.inflate(R.menu.editor_actionmode, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        if (item?.itemId == null) return false
        val richTextSpan = RichTextSpanType.getSpanType(item.itemId)
        if (richTextSpan != null)
            this.applySpan(richTextSpan)
        return richTextSpan != null
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
    }

}