package fr.asdl.paperbee.storage.v1

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.CharacterStyle
import fr.asdl.paperbee.IndexRange
import fr.asdl.paperbee.storage.SpanProcessor
import fr.asdl.paperbee.view.RichTextSpan

class SpanProcessorImpl : SpanProcessor {

    private val escapeRegex = Regex("[\\\\<>]")
    private val unEscapeRegex = Regex("\\\\[\\\\<>]")
    private val deSerialRegex = Regex(
        "((?<!\\\\)<(?<tag>[^/]*?)( (.*?))?(?<!\\\\)>)(.*?)((?<!\\\\)</\\k<tag>(?<!\\\\)>)",
        RegexOption.DOT_MATCHES_ALL
    )

    private fun find(string: String, regex: Regex): List<MatchResult> {
        val data = arrayListOf<MatchResult>()
        var result = regex.find(string)
        while (result != null) {
            data.add(result)
            result = result.next()
        }
        return data
    }

    private fun escapeText(input: String): String {
        return escapeRegex.replace(input) { "\\${it.value}" }
    }

    private fun unEscapeText(input: String): String {
        return unEscapeRegex.replace(input) { it.value.drop(1) }
    }

    override fun serialize(context: Context, editable: Editable): String {
        val serialized = StringBuilder(escapeText(editable.toString()))
        val mappedSpan = hashMapOf<IndexRange, RichTextSpan>()
        editable.getSpans(0, editable.length, CharacterStyle::class.java).forEach {
            mappedSpan[IndexRange(editable.getSpanStart(it), editable.getSpanEnd(it))] =
                RichTextSpan(it, context)
        }
        fun appendTag(tag: String, pos: Int) {
            serialized.insert(pos, tag)
            mappedSpan.keys.forEach {
                it.shift(
                    if (it.start >= pos) tag.length else 0,
                    if (it.end >= pos) tag.length else 0
                )
            }
        }
        for (entry in mappedSpan) {
            val extra = entry.value.getExtraAsString()
            appendTag("<${entry.value.type.delimiter}${if (extra != null) " ${escapeText(extra)}" else ""}>", entry.key.start)
            appendTag("</${entry.value.type.delimiter}>", entry.key.end)
        }
        return serialized.toString()
    }

    override fun deserialize(context: Context, string: String): Editable {
        var clearString = string
        val mappedSpan = hashMapOf<IndexRange, RichTextSpan>()
        val pendingRemoval = arrayListOf<IndexRange>()
        fun removeTag(range: IndexRange) {
            clearString = clearString.removeRange(IntRange(range.start, range.end))
            pendingRemoval.forEach {
                if (it !== range)
                    it.shift(
                        if (it.start >= range.start) -range.length else 0,
                        if (it.end >= range.end) -range.length else 0
                    )
            }
            mappedSpan.keys.forEach { r ->
                r.shift(
                    if (r.start >= range.start) -range.length else 0,
                    if (r.end >= range.end) -range.length else 0
                )
            }
        }

        fun parse(parsable: String, indexOffset: Int = 0) {
            this.find(parsable, deSerialRegex).forEach {
                it.groups.apply {
                    if (this[5] == null) return@apply
                    mappedSpan[IndexRange(this[5]!!.range.first + indexOffset, this[5]!!.range.last + indexOffset)] =
                        RichTextSpan(this[2]?.value ?: return@apply, unEscapeText(this[4]?.value ?: ""))
                    pendingRemoval.add(IndexRange(this[1]!!.range.first + indexOffset, this[1]!!.range.last + indexOffset))
                    pendingRemoval.add(IndexRange(this[6]!!.range.first + indexOffset, this[6]!!.range.last + indexOffset))
                    parse(this[5]!!.value, this[5]!!.range.first + indexOffset)
                }
            }
        }
        parse(clearString)
        pendingRemoval.forEach { removeTag(it) }
        val editable = SpannableStringBuilder(unEscapeText(clearString))
        for (entry in mappedSpan)
            editable.setSpan(
                entry.value.getSpan(context),
                entry.key.start,
                entry.key.end + 1,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        return editable
    }

}