package fr.asdl.minder.note

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

@Suppress("EXPERIMENTAL_API_USAGE")
@Serializer(forClass = Note::class)
class NoteSerializer : KSerializer<Note> {

    @InternalSerializationApi
    override val descriptor = buildClassSerialDescriptor("fr.asdl.minder.serial.dataholderlist") {
        element("title", String.serializer().descriptor)
        element("id", Int.serializer().descriptor)
        element("creationStamp", Long.serializer().descriptor)
        element("items", listSerialDescriptor(NotePart::class.serializer().descriptor))
    }

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: Note) {
        with (encoder.beginStructure(descriptor)) {
            for (i in 0 until descriptor.elementsCount)
                when(descriptor.getElementName(i)) {
                    "title" -> this.encodeStringElement(descriptor, i, value.title)
                    "id" -> if (value.id != null) this.encodeIntElement(descriptor, i, value.id!!)
                    "creationStamp" -> this.encodeLongElement(descriptor, i, value.creationStamp)
                    "items" -> this.encodeSerializableElement(descriptor, i, ListSerializer(NotePart::class.serializer()), value.retrieveContent())
                }
            this.endStructure(descriptor)
        }
    }

    @InternalSerializationApi
    override fun deserialize(decoder: Decoder): Note {
        with (decoder.beginStructure(descriptor)) {
            var title: String? = null
            var id: Int? = null
            var creationStamp: Long? = null
            var items: List<NotePart>? = null
            loop@ while (true) {
                when(val index = decodeElementIndex(descriptor)) {
                    descriptor.getElementIndex("title") -> title = this.decodeStringElement(descriptor, index)
                    descriptor.getElementIndex("id") -> id = this.decodeIntElement(descriptor, index)
                    descriptor.getElementIndex("creationStamp") -> creationStamp = this.decodeLongElement(descriptor, index)
                    descriptor.getElementIndex("items") -> items = this.decodeSerializableElement(descriptor, index, ListSerializer(NotePart::class.serializer()))
                    CompositeDecoder.DECODE_DONE -> break@loop
                }
            }
            this.endStructure(descriptor)
            val note = Note(title!!, LinkedList(items!!), null)
            note.id = id
            note.creationStamp = creationStamp!!
            return note
        }
    }
}