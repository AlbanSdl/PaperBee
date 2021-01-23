package fr.asdl.paperbee.storage.v1

import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.CharacterStyle
import fr.asdl.paperbee.IndexRange
import fr.asdl.paperbee.storage.SpanProcessor
import fr.asdl.paperbee.view.RichTextSpanType

class SpanProcessorImpl : SpanProcessor {

    private val escapeRegex = Regex("[\\\\<>]")
    private val unEscapeRegex = Regex("\\\\[\\\\<>]")
    private val deSerialRegex = Regex(
        "((?<!\\\\)<(?<tag>[^/]*?)(?<!\\\\)>)(.*?)((?<!\\\\)</\\k<tag>(?<!\\\\)>)",
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

    override fun serialize(editable: Editable): String {
        val serialized = StringBuilder(escapeText(editable.toString()))
        val mappedSpan = hashMapOf<IndexRange, RichTextSpanType>()
        editable.getSpans(0, editable.length, CharacterStyle::class.java).forEach {
            mappedSpan[IndexRange(editable.getSpanStart(it), editable.getSpanEnd(it))] =
                RichTextSpanType.getSpanType(it) ?: return@forEach
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
            appendTag("<${entry.value.delimiter}>", entry.key.start)
            appendTag("</${entry.value.delimiter}>", entry.key.end)
        }
        return serialized.toString()
    }

    override fun deserialize(string: String): Editable {
        var clearString = string
        val mappedSpan = hashMapOf<IndexRange, RichTextSpanType>()
        val pendingRemoval = arrayListOf<MatchGroup>()
        var charRemoved = 0
        fun removeTag(matchGroup: MatchGroup) {
            clearString = clearString.removeRange(
                IntRange(
                    matchGroup.range.first - charRemoved,
                    matchGroup.range.last - charRemoved
                )
            )
            mappedSpan.keys.forEach { range ->
                range.shift(
                    if (range.start >= matchGroup.range.first - charRemoved) -matchGroup.value.length else 0,
                    if (range.end >= matchGroup.range.last - charRemoved) -matchGroup.value.length else 0
                )
            }
            charRemoved += matchGroup.value.length
        }
        this.find(clearString, deSerialRegex).forEach {
            it.groups.apply {
                mappedSpan[IndexRange(this[3]!!.range.first, this[3]!!.range.last)] =
                    RichTextSpanType.getSpanType(this[2]?.value ?: return@forEach)
                        ?: return@forEach
                pendingRemoval.add(this[1]!!); pendingRemoval.add(this[4]!!)
            }
        }
        pendingRemoval.forEach { removeTag(it) }
        val editable = SpannableStringBuilder(unEscapeText(clearString))
        for (entry in mappedSpan)
            editable.setSpan(
                entry.value.getSpan(),
                entry.key.start,
                entry.key.end + 1,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        return editable
    }

}