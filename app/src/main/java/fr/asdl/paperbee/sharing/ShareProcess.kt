package fr.asdl.paperbee.sharing

import fr.asdl.paperbee.exceptions.IncompatibleVersionException
import fr.asdl.paperbee.storage.v1.decodeBase64
import fr.asdl.paperbee.storage.v1.deserialize
import fr.asdl.paperbee.storage.v1.encodeBase64
import fr.asdl.paperbee.storage.v1.serialize
import fr.asdl.paperbee.view.sentient.DataHolder

class ShareProcess : SharingFactory<DataHolder>() {

    override fun toBytes(sharable: DataHolder, encryptionKey: String?): ByteArray {
        return serialize(sharable) { this.encrypt(encryptionKey, it.toByteArray()).encodeBase64() }.toByteArray()
    }

    override fun fromBytes(bytes: ByteArray, protocolVersion: Int, encryptionKey: String?): DataHolder {
        if (protocolVersion != this.writingProtocolVersion()) throw IncompatibleVersionException()
        return deserialize(bytes) { String(this.decrypt(encryptionKey, it.decodeBase64())) }
    }

    override fun writingProtocolVersion(): Int = 1

}