package fr.asdl.paperbee.storage.v1

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import fr.asdl.paperbee.IndexRange
import fr.asdl.paperbee.storage.SpanProcessor
import fr.asdl.paperbee.view.RichSpannable
import fr.asdl.paperbee.view.RichTextSpan

class SpanProcessorImpl : SpanProcessor {

    private val escapeRegex = Regex("[\\\\<>]")
    private val unEscapeRegex = Regex("\\\\[\\\\<>]")
    private val deSerialRegexTagStart = Regex("(?<!\\\\)<([^/]*?)( (.*?))?(?<!\\\\)>")
    private val deSerialRegexTagEnd = Regex("(?<!\\\\)</([^/]*?)(?<!\\\\)>")

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

    private fun mapByPairs(starts: Map<IndexRange, RichTextSpan>, ends: Map<IndexRange, String>): Map<IndexRange, RichTextSpan> {
        val spans = hashMapOf<IndexRange, RichTextSpan>()
        val sortedEnds = ends.toSortedMap(compareBy { it.start })
        starts.forEach root@{ s ->
            sortedEnds.forEach {
                if (it.value == s.value.type.delimiter && it.key.start > s.key.start) {
                    if (s.key.end + 1 < it.key.start - 1) {
                        spans[IndexRange(s.key.end + 1, it.key.start - 1)] = s.value
                        return@root
                    }
                }
            }
        }
        return spans
    }

    override fun serialize(context: Context, editable: RichSpannable): String {
        val serialized = StringBuilder(escapeText(editable.toString()))
        val mappedSpan = hashMapOf<IndexRange, RichTextSpan>()
        editable.getContainedSpans(0, editable.length, null).forEach {
            val range = IndexRange(editable.getSpanStart(it), editable.getSpanEnd(it))
            if (range.length > 1)
                mappedSpan[range] = it
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
                    if (r.start > range.start) -range.length else 0,
                    if (r.end >= range.end) -range.length else 0
                )
            }
        }

        val startTags = hashMapOf<IndexRange, RichTextSpan>()
        val endTags = hashMapOf<IndexRange, String>()
        this.find(clearString, deSerialRegexTagStart).forEach {
            startTags[IndexRange(it.groups[0]!!.range.first, it.groups[0]!!.range.last)] =
                RichTextSpan(it.groups[1]?.value ?: return@forEach, it.groups[3]?.value)
        }
        this.find(clearString, deSerialRegexTagEnd).forEach {
            endTags[IndexRange(it.groups[0]!!.range.first, it.groups[0]!!.range.last)] = it.groups[1]?.value ?: return@forEach
        }
        pendingRemoval.addAll(startTags.keys)
        pendingRemoval.addAll(endTags.keys)
        mappedSpan.putAll(this.mapByPairs(startTags, endTags))

        pendingRemoval.forEach { removeTag(it) }
        val editable = SpannableStringBuilder(unEscapeText(clearString))
        for (entry in mappedSpan)
            editable.setSpan(
                entry.value.getSpan(context),
                entry.key.start,
                entry.key.end + 1,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
        return editable
    }

}