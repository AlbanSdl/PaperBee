package fr.asdl.paperbee.view

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.style.CharacterStyle
import fr.asdl.paperbee.storage.SpanProcessor
import fr.asdl.paperbee.storage.v1.SpanProcessorImpl

class RichSpannable private constructor(private val context: Context, clearedText: Editable) :
    SpannableStringBuilder(clearedText) {

    constructor(context: Context, origin: String) : this(context, spanProcessor.deserialize(context, origin)) {
        super.getSpans(0, this.length, CharacterStyle::class.java).forEach {
            this.spans.add(RichTextSpan(it, context))
        }
    }

    /**
     * Returns a new updated [RichSpannable] with [content] as visible text and keeping the
     * spans of its previous form (known as [from])
     */
    constructor(from: RichSpannable, content: Editable) : this(from.context, content) {
        this.spans.clear()
        this.spans.addAll(from.spans)
    }

    private val spans = arrayListOf<RichTextSpan>()

    fun getSpans(queryStart: Int, queryEnd: Int, type: RichTextSpanType?): Array<RichTextSpan> {
        return spans.filter {
            if (type == null || it.type === type) {
                val sStart = getSpanStart(it)
                val sEnd = getSpanEnd(it)
                if (sStart >= 0 && sEnd >= 0 && (sStart in queryStart..queryEnd || sEnd in queryStart..queryEnd))
                    return@filter true
            }
            false
        }.toTypedArray()
    }

    @Deprecated("Spans should not be queried this way. This includes spans added by " +
            "third party apps such as temporary spans from auto-corrector",
        ReplaceWith("getSpans(queryStart, queryEnd, type)"))
    override fun <T : Any?> getSpans(queryStart: Int, queryEnd: Int, kind: Class<T>?): Array<T> {
        return super.getSpans(queryStart, queryEnd, kind)
    }

    fun setSpan(span: RichTextSpan?, start: Int, end: Int, flags: Int) {
        if (span != null) {
            spans.add(span)
            return super.setSpan(span.getSpan(context), start, end, flags)
        }
    }

    @Deprecated("Should not use raw spans")
    override fun setSpan(what: Any?, start: Int, end: Int, flags: Int) {
        if (what is RichTextSpan)
            return this.setSpan(what, start, end, flags)
        return super.setSpan(what, start, end, flags)
    }

    fun removeSpan(span: RichTextSpan?) {
        if (span == null) return
        spans.remove(span)
        return super.removeSpan(span.getSpan(null))
    }

    @Deprecated("Should not use raw spans")
    override fun removeSpan(what: Any?) {
        if (what is RichTextSpan)
            return this.removeSpan(what)
        return super.removeSpan(what)
    }

    fun getSpanStart(span: RichTextSpan?): Int {
        return super.getSpanStart(span?.getSpan(context))
    }

    @Deprecated("Should not use raw spans", ReplaceWith("getSpanStart(span as RichTextSpan)"))
    override fun getSpanStart(what: Any?): Int {
        return super.getSpanStart(what)
    }

    fun getSpanEnd(span: RichTextSpan?): Int {
        return super.getSpanEnd(span?.getSpan(context))
    }

    @Deprecated("Should not use raw spans", ReplaceWith("getSpanEnd(span as RichTextSpan)"))
    override fun getSpanEnd(what: Any?): Int {
        return super.getSpanEnd(what)
    }

    override fun clearSpans() {
        this.spans.clear()
        return super.clearSpans()
    }

    fun toRawString(): String {
        return spanProcessor.serialize(this.context, this)
    }

    companion object {
        private val spanProcessor: SpanProcessor = SpanProcessorImpl()
    }

}