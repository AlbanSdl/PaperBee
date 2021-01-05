package fr.asdl.paperbee.sharing

import fr.asdl.paperbee.exceptions.IncompatibleVersionException
import fr.asdl.paperbee.storage.v1.deserialize
import fr.asdl.paperbee.storage.v1.serialize
import fr.asdl.paperbee.view.sentient.DataHolder
import java.nio.charset.StandardCharsets

class ShareProcess : SharingFactory<DataHolder>() {

    override fun toBytes(sharable: DataHolder, encryptionKey: String?): ByteArray {
        return serialize(sharable) {
            this.encrypt(encryptionKey, it.toByteArray(StandardCharsets.UTF_8)).decodeToString()
        }.toByteArray(StandardCharsets.UTF_8)
    }

    override fun fromBytes(bytes: ByteArray, protocolVersion: Int, encryptionKey: String?): DataHolder {
        if (protocolVersion != this.writingProtocolVersion()) throw IncompatibleVersionException()
        return deserialize(String(bytes, StandardCharsets.UTF_8)) {
            this.decrypt(encryptionKey, it.toByteArray(StandardCharsets.UTF_8)).decodeToString()
        }
    }

    override fun writingProtocolVersion(): Int = 1

}