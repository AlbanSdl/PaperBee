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
@Serializer(forClass = Notable::class)
class NoteSerializer : KSerializer<Notable<*>> {

    @InternalSerializationApi
    override val descriptor = buildClassSerialDescriptor("fr.asdl.minder.serial.dataholderlist") {
        element("title", String.serializer().descriptor)
        element("id", Int.serializer().descriptor)
        element("order", Int.serializer().descriptor)
        element("items", listSerialDescriptor(NotePart::class.serializer().descriptor))
        element("parentId", Int.serializer().descriptor)
    }

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: Notable<*>) {
        with (encoder.beginStructure(descriptor)) {
            for (i in 0 until descriptor.elementsCount)
                when(descriptor.getElementName(i)) {
                    "title" -> this.encodeStringElement(descriptor, i, value.title)
                    "id" -> if (value.id != null) this.encodeIntElement(descriptor, i, value.id!!)
                    "order" -> this.encodeIntElement(descriptor, i, value.order)
                    "items" -> if (value is Note) this.encodeSerializableElement(descriptor, i, ListSerializer(NotePart::class.serializer()), value.getContents())
                    "parentId" -> if (value.parentId != null) this.encodeIntElement(descriptor, i, value.parentId!!)
                }
            this.endStructure(descriptor)
        }
    }

    @InternalSerializationApi
    override fun deserialize(decoder: Decoder): Notable<*> {
        with (decoder.beginStructure(descriptor)) {
            var title: String? = null
            var id: Int? = null
            var order: Int? = null
            var items: List<NotePart>? = null
            var parentId: Int = -1
            loop@ while (true) {
                when(val index = decodeElementIndex(descriptor)) {
                    descriptor.getElementIndex("title") -> title = this.decodeStringElement(descriptor, index)
                    descriptor.getElementIndex("id") -> id = this.decodeIntElement(descriptor, index)
                    descriptor.getElementIndex("order") -> order = this.decodeIntElement(descriptor, index)
                    descriptor.getElementIndex("items") -> items = this.decodeSerializableElement(descriptor, index, ListSerializer(NotePart::class.serializer()))
                    descriptor.getElementIndex("parentId") -> parentId = this.decodeIntElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break@loop
                }
            }
            this.endStructure(descriptor)

            return if (items != null) {
                val note = Note(title!!, null, LinkedList(items), null, parentId)
                note.id = id
                note.order = order!!
                note
            } else {
                val folder = NoteFolder(title!!, null, LinkedList(), null, parentId)
                folder.id = id
                folder.order = order!!
                folder
            }
        }
    }
}