package fr.asdl.paperbee.sharing

import fr.asdl.paperbee.exceptions.IncompatibleVersionException
import fr.asdl.paperbee.note.Notable
import fr.asdl.paperbee.note.NoteSerializer
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

class ShareProcess : SharingFactory<Notable<*>>() {

    companion object {
        private val serializer = NoteSerializer()
    }

    override fun toBytes(sharable: Notable<*>): ByteArray {
        return Json.encodeToString(serializer, sharable).toByteArray(StandardCharsets.UTF_8)
    }

    override fun fromBytes(bytes: ByteArray, protocolVersion: Int): Notable<*> {
        if (protocolVersion > this.writingProtocolVersion()) throw IncompatibleVersionException()
        return Json.decodeFromString(serializer, String(bytes, StandardCharsets.UTF_8))
    }

    override fun writingProtocolVersion(): Int = 1

}