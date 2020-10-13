package fr.asdl.minder.note

import fr.asdl.minder.note.NoteManager.Companion.ROOT_ID
import fr.asdl.minder.view.options.Color
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
        element("color", String.serializer().descriptor)
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
                    "color" -> if (value.color != null) this.encodeStringElement(descriptor, i, value.color!!.tag)
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
            var parentId: Int = ROOT_ID
            var color: Color? = null
            loop@ while (true) {
                when(val index = decodeElementIndex(descriptor)) {
                    descriptor.getElementIndex("title") -> title = this.decodeStringElement(descriptor, index)
                    descriptor.getElementIndex("id") -> id = this.decodeIntElement(descriptor, index)
                    descriptor.getElementIndex("order") -> order = this.decodeIntElement(descriptor, index)
                    descriptor.getElementIndex("items") -> items = this.decodeSerializableElement(descriptor, index, ListSerializer(NotePart::class.serializer()))
                    descriptor.getElementIndex("parentId") -> parentId = this.decodeIntElement(descriptor, index)
                    descriptor.getElementIndex("color") -> color = Color.getFromTag(this.decodeStringElement(descriptor, index))
                    CompositeDecoder.DECODE_DONE -> break@loop
                }
            }
            this.endStructure(descriptor)

            val notable =
                if (items != null) Note(title!!, null, LinkedList(items), null, parentId)
                else NoteFolder(title!!, null, LinkedList(), null, parentId)
            notable.id = id
            notable.order = order!!
            notable.color = color
            return notable
        }
    }
}