package fr.asdl.paperbee.storage.v1

import android.util.Base64.*
import fr.asdl.paperbee.exceptions.IncompatibleVersionException
import fr.asdl.paperbee.exceptions.WrongPasswordException
import fr.asdl.paperbee.note.*
import fr.asdl.paperbee.storage.SpanProcessor
import fr.asdl.paperbee.view.options.Color
import fr.asdl.paperbee.view.sentient.DataHolder
import java.io.NotSerializableException


private val spanProcessor = object : SpanProcessor {}
private val escapeRegex = Regex("[\\\\\"]")
private val unEscapeRegex = Regex("\\\\[\\\\\"]")
private val deSerialRegex = Regex("(?<!\\\\)\".*?(?<!\\\\)\"", RegexOption.DOT_MATCHES_ALL)
private fun escapeText(input: String): String {
    return escapeRegex.replace(input) { "\\${it.value}" }
}

private fun unEscapeText(input: String): String {
    return unEscapeRegex.replace(input) { it.value.drop(1) }
}

fun serialize(dataHolder: DataHolder, enc: (str: String) -> String): String {
    return with(NotableContract.DataHolderType.getType(dataHolder)) {
        when (this) {
            NotableContract.DataHolderType.NOTE, NotableContract.DataHolderType.FOLDER ->
                "[\"${escapeText(enc(dataHolder.id.toString()))}\",\"${escapeText(enc(dataHolder.order.toString()))}\",\"${
                    escapeText(enc(dataHolder.parentId.toString()))
                }\",\"${escapeText(enc(this.id.toString()))}\",\"${escapeText(enc((dataHolder as Notable<*>).title))}\",\"${
                    escapeText(enc(dataHolder.color?.tag.toString()))
                }\"]"
            NotableContract.DataHolderType.TEXT -> "[\"${escapeText(enc(dataHolder.id.toString()))}\",\"${
                escapeText(
                    enc(
                        dataHolder.order.toString()
                    )
                )
            }\",\"${
                escapeText(enc(dataHolder.parentId.toString()))
            }\",\"${escapeText(enc(this.id.toString()))}\",\"${escapeText(enc(spanProcessor.serialize((dataHolder as TextNotePart).content)))}\"]"
            NotableContract.DataHolderType.CHECKBOX -> "[\"${escapeText(enc(dataHolder.id.toString()))}\",\"${
                escapeText(
                    enc(
                        dataHolder.order.toString()
                    )
                )
            }\",\"${
                escapeText(enc(dataHolder.parentId.toString()))
            }\",\"${escapeText(enc(this.id.toString()))}\",\"${escapeText(enc(spanProcessor.serialize((dataHolder as TextNotePart).content)))}\",\"${
                escapeText(enc((dataHolder as CheckableNotePart).checked.toString()))
            }\"]"
            else -> throw NotSerializableException()
        }
    }
}

fun deserialize(content: ByteArray, dec: (str: String) -> String): DataHolder {
    val data = arrayListOf<String>()
    var result = deSerialRegex.find(String(content))
    while (result != null) {
        data.add(dec(unEscapeText(result.value.substring(1, result.value.length - 1))))
        result = result.next()
    }
    if (data.size < 5) throw IncompatibleVersionException()
    val holder = when (NotableContract.DataHolderType.fromInt(getInt(data[3]) ?: throw WrongPasswordException())) {
        NotableContract.DataHolderType.FOLDER -> NoteFolder()
        NotableContract.DataHolderType.NOTE -> Note()
        NotableContract.DataHolderType.TEXT -> NoteText(spanProcessor.deserialize(data[4]))
        NotableContract.DataHolderType.CHECKBOX -> if (data.size > 5) NoteCheckBoxable(
            spanProcessor.deserialize(data[4]),
            data[5].toBoolean()
        ) else throw IncompatibleVersionException()
        else -> throw IncompatibleVersionException()
    }
    holder.initializeId(getInt(data[0]) ?: throw IncompatibleVersionException())
    holder.order = getInt(data[1]) ?: throw IncompatibleVersionException()
    holder.parentId = getInt(data[2])
    if (holder is Notable<*>) {
        holder.title = data[4]
        if (data.size > 5) holder.color = Color.getFromTag(data[5])
    }
    return holder
}

fun getInt(str: String): Int? {
    return try {
        str.toInt()
    } catch (e: NumberFormatException) {
        null
    }
}

internal fun String.decodeBase64(): ByteArray {
    return decode(this, DEFAULT)
}

internal fun ByteArray.encodeBase64(): String {
    return encodeToString(this, NO_WRAP or NO_PADDING)
}