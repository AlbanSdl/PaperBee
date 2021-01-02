package fr.asdl.paperbee.sharing

import fr.asdl.paperbee.exceptions.IncompatibleVersionException
import fr.asdl.paperbee.note.Notable

class ShareProcess : SharingFactory<Notable<*>>() {

    override fun toBytes(sharable: Notable<*>): ByteArray {
        TODO()
        //return Json.encodeToString(sharable).toByteArray(StandardCharsets.UTF_8)
    }

    override fun fromBytes(bytes: ByteArray, protocolVersion: Int): Notable<*> {
        if (protocolVersion > this.writingProtocolVersion()) throw IncompatibleVersionException()
        //return Json.decodeFromString(String(bytes, StandardCharsets.UTF_8))
        TODO()
    }

    override fun writingProtocolVersion(): Int = 1

}