package fr.asdl.minder.sharing

import fr.asdl.minder.note.Notable
import fr.asdl.minder.note.NoteSerializer
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
        return Json.decodeFromString(serializer, String(bytes, StandardCharsets.UTF_8))
    }

    override fun writingProtocolVersion(): Int = 1

}