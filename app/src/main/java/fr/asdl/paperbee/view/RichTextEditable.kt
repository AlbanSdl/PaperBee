package fr.asdl.paperbee.view

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatEditText
import fr.asdl.paperbee.IndexRange
import fr.asdl.paperbee.note.TextNotePart

abstract class RichTextEditable<T: TextNotePart>(context: Context, attributeSet: AttributeSet) :
    AppCompatEditText(context, attributeSet), TextWatcher, UrlSpanLongClickListener {

    private var mAttachedElement: T? = null
    private var mAttachedSpannable: RichSpannable? = null

    init {
        this.movementMethod = LongClickLinkMovementMethod.instance
    }

    final override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        this.onSelectionUpdated(this.hasSelection(), if (selStart < selEnd) this.getSelectionSpans() else arrayOf())
    }

    protected abstract fun onSelectionUpdated(hasSelection: Boolean, selectionSpans: Array<RichTextSpan>)

    /**
     * Used to attach an object to the [RichTextEditable]. An eventual attached object
     * will always be detached before this operation, using [detach]
     */
    fun attach(attachedElement: T) {
        this.detach()
        mAttachedElement = attachedElement
        mAttachedSpannable = RichSpannable(context, attachedElement.content)
        this.text = mAttachedSpannable
        this.addTextChangedListener(this)
    }

    /**
     * Removes the current listener of the rich text editor.
     * This is may be seen as a replacement for RichTextEditable.attach(null)
     */
    fun detach() {
        this.removeTextChangedListener(this)
        this.mAttachedElement = null
        this.mAttachedSpannable = null
    }

    protected fun getSelectionSpans(type: RichTextSpanType? = null): Array<RichTextSpan> {
        return mAttachedSpannable?.getContainingSpans(selectionStart, selectionEnd, type) ?: arrayOf()
    }

    private fun updateContent() {
        val selection = IndexRange(selectionStart, selectionEnd)
        text = this.mAttachedSpannable!!
        setSelection(selection.start, selection.end)
        this.performLongClick() // Keeping action mode ready on click
        this.onSelectionChanged(selectionStart, selectionEnd)
        onTextUpdated(this.mAttachedSpannable!!, mAttachedElement!!)
    }

    /**
     * Toggles the chosen style to the selection. Can be used to remove the chosen style !
     * This method checks whether the selection is not empty using [hasSelection]
     * The new style is applied with the [Spannable.SPAN_EXCLUSIVE_INCLUSIVE] flag meaning any
     * inserted char right after the span will be included too.
     */
    protected fun applySpan(span: RichTextSpan) {
        if (span.type.hasExtra && span.getExtraAsString() == null && span.type != RichTextSpanType.LINK) {
            Log.wtf(javaClass.simpleName, "Cannot use null extra !")
            return
        }

        fun applySpan(span: RichTextSpan, range: IndexRange) {
            if (span.type == RichTextSpanType.LINK && span.extra == null) return
            mAttachedSpannable!!.setSpan(span, range.start, range.end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        }

        if (this.hasSelection()) {
            val selection = IndexRange(selectionStart, selectionEnd)
            val pendingSpans = hashMapOf<IndexRange, RichTextSpan>()
            for (i in this.getSelectionSpans(span.type))
                this.mAttachedSpannable!!.apply {
                    pendingSpans[IndexRange(this.getSpanStart(i), this.getSpanEnd(i))] = i
                    removeSpan(i)
                }

            val removal = pendingSpans.entries.find {
                it.key.contains(selection) && it.value.extra == span.extra
            }?.key
            if (removal == null) pendingSpans[selection] = span
            else {
                if (selection.contains(removal)) pendingSpans.remove(removal)
                else {
                    var used = false
                    if (removal.start < selectionStart) {
                        pendingSpans[IndexRange(removal.start, selectionStart)] = span
                        used = true
                    }
                    if (removal.end > selectionEnd)
                        pendingSpans[IndexRange(selectionEnd, removal.end)] = if (used) span.copy() else span
                }
            }

            IndexRange.group(*pendingSpans.filter { entry -> entry.value.extra == span.extra }
                .map { it.key }.toTypedArray()).forEach {
                applySpan(span.copy(), it)
            }

            for (r in pendingSpans.filter { entry -> entry.value.extra != span.extra }) {
                if (r.key.and(selection)?.length?.compareTo(0) == 1) {
                    if (selection.contains(r.key))
                        continue
                    else if (r.key.contains(selection)) {
                        applySpan(r.value.copy(), IndexRange(selectionEnd, r.key.end))
                        r.key.shift(0, selectionStart - r.key.end)
                    }
                    else if (r.key.start in selectionStart..selectionEnd)
                        r.key.shift(selectionEnd - r.key.start, 0)
                    else r.key.shift(0, selectionStart - r.key.end)
                }
                if (r.key.length > 0) applySpan(r.value, r.key)
            }

            this.updateContent()
        }
    }

    /**
     * This method is called on every update of the text.
     * May be used to notify and enqueue a save to a persistent form
     */
    protected abstract fun onTextUpdated(updated: RichSpannable, attachedElement: T)

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    @CallSuper
    override fun afterTextChanged(s: Editable?) {
        if (mAttachedElement is T && s != null) {
            mAttachedSpannable = RichSpannable(this.mAttachedSpannable!!, s)
            this.onTextUpdated(mAttachedSpannable!!, mAttachedElement!!)
        }
    }

    protected fun getSpanStart(it: RichTextSpan): Int {
        return this.mAttachedSpannable?.getSpanStart(it) ?: -1
    }

    protected fun getSpanEnd(it: RichTextSpan): Int {
        return this.mAttachedSpannable?.getSpanEnd(it) ?: -1
    }

}
