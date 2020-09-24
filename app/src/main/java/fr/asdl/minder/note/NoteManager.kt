package fr.asdl.minder.note

import android.content.Context
import android.os.Build
import fr.asdl.minder.IntAllocator
import fr.asdl.minder.R
import fr.asdl.minder.preferences.SavedDataDirectory
import fr.asdl.minder.view.DataHolder
import fr.asdl.minder.view.DataHolderList
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class NoteManager(private val context: Context) : DataHolderList<Note>() {
    private val dataDirectory = SavedDataDirectory(this.context.getString(R.string.notes_directory_name), context)
    private val notes = LinkedList<Note>()
    private val idAllocator = IntAllocator(this.getNoteIds())
    private val serializer = NoteSerializer()

    init {
        dataDirectory.loadData(this, this.serializer)
    }

    override fun retrieveContent(): LinkedList<Note> {
        return notes
    }

    override fun save(element: Note) {
        if (element.id == null) {
            element.id = idAllocator.allocate()
        }
        if (element.noteManager == null) {
            element.noteManager = this
            idAllocator.forceAllocate(element.id!!)
        }
        this.dataDirectory.saveDataAsync(element, serializer = this.serializer)
    }

    override fun delete(element: Note) {
        if (element.id == null) return
        this.idAllocator.release(element.id!!)
        this.dataDirectory.saveDataAsync(id = element.id!!, serializer = this.serializer)
    }

    private fun getNoteIds(): ArrayList<Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ArrayList(notes.stream().map { it.id!! }.collect(Collectors.toList()))
        } else {
            val r = ArrayList<Int>()
            for (note in notes) r.add(note.id!!)
            r
        }
    }
}

/**
 * The structure of a note, containing a title and contents.
 */
class Note(var title: String,
           private val items: LinkedList<NotePart> = LinkedList(),
           var noteManager: DataHolderList<Note>?
) : DataHolder, DataHolderList<NotePart>() {

    override var id: Int? = null
    override var creationStamp: Long = Date().time

    private fun save() {
        if (this.noteManager != null)
            if (this.id == null) noteManager!!.add(this) else noteManager!!.update(this)
    }

    override fun retrieveContent(): LinkedList<NotePart> {
        return this.items
    }

    override fun save(element: NotePart) {
        this.save()
    }

    override fun delete(element: NotePart) {
        this.save()
    }
}

/**
 * A part of a note. This can be text, image, checkbox-indented item, or whatever implements
 * this interface. It must be displayable by the Note and its recycler view.
 *
 * This will implement DataHolder in the future and will be displayed in the recycler view of
 * its Note ↑
 */
@Serializable
sealed class NotePart(override val creationStamp: Long = Date().time) : DataHolder

@Serializable
class NoteText(val content: String, override val id: Int) : NotePart()

@Serializable
class NoteCheckBoxable(val content: String, override val id: Int) : NotePart()

@ExperimentalSerializationApi
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
                    "id" -> this.encodeIntElement(descriptor, i, value.id!!)
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
                    DECODE_DONE -> break@loop
                }
            }
            this.endStructure(descriptor)
            val note = Note(title!!, LinkedList(items!!), null)
            note.id = id!!
            note.creationStamp = creationStamp!!
            return note
        }
    }
}