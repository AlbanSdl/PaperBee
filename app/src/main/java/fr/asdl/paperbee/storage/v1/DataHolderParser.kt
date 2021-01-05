package fr.asdl.paperbee.storage.v1

import fr.asdl.paperbee.exceptions.IncompatibleVersionException
import fr.asdl.paperbee.note.*
import fr.asdl.paperbee.view.options.Color
import fr.asdl.paperbee.view.sentient.DataHolder
import java.io.NotSerializableException


private val escapeRegex = Regex("[\\\\\"]")
private val unEscapeRegex = Regex("\\\\.")
private val deSerialRegex = Regex("(?<!\\\\)\".*?(?<!\\\\)\"")
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
            }\",\"${escapeText(enc(this.id.toString()))}\",\"${escapeText(enc((dataHolder as TextNotePart).content))}\"]"
            NotableContract.DataHolderType.CHECKBOX -> "[\"${escapeText(enc(dataHolder.id.toString()))}\",\"${
                escapeText(
                    enc(
                        dataHolder.order.toString()
                    )
                )
            }\",\"${
                escapeText(enc(dataHolder.parentId.toString()))
            }\",\"${escapeText(enc(this.id.toString()))}\",\"${escapeText(enc((dataHolder as TextNotePart).content))}\",\"${
                escapeText(enc((dataHolder as CheckableNotePart).checked.toString()))
            }\"]"
            else -> throw NotSerializableException()
        }
    }
}

fun deserialize(string: String, dec: (str: String) -> String): DataHolder {
    val data = arrayListOf<String>()
    var result = deSerialRegex.find(string)
    while (result != null) {
        data.add(dec(unEscapeText(result.value)))
        result = result.next()
    }
    if (data.size < 5) throw IncompatibleVersionException()
    val holder = when (NotableContract.DataHolderType.fromInt(data[3].toInt())) {
        NotableContract.DataHolderType.FOLDER -> NoteFolder()
        NotableContract.DataHolderType.NOTE -> Note()
        NotableContract.DataHolderType.TEXT -> NoteText(data[4])
        NotableContract.DataHolderType.CHECKBOX -> if (data.size > 5) NoteCheckBoxable(
            data[4],
            data[5].toBoolean()
        ) else throw IncompatibleVersionException()
        else -> throw IncompatibleVersionException()
    }
    holder.initializeId(data[0].toInt())
    holder.order = data[1].toInt()
    holder.parentId = data[2].toInt()
    if (holder is Notable<*>) {
        holder.title = data[4]
        if (data.size > 5) holder.color = Color.getFromTag(data[5])
    }
    return holder
}