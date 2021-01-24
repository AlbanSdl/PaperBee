package fr.asdl.paperbee.sharing

import android.content.Context
import fr.asdl.paperbee.exceptions.IncompatibleVersionException
import fr.asdl.paperbee.storage.v1.decodeBase64
import fr.asdl.paperbee.storage.v1.deserialize
import fr.asdl.paperbee.storage.v1.encodeBase64
import fr.asdl.paperbee.storage.v1.serialize
import fr.asdl.paperbee.view.sentient.DataHolder

class ShareProcess : SharingFactory<DataHolder>() {

    override fun toBytes(context: Context, sharable: DataHolder, encryptionKey: String?): ByteArray {
        return serialize(context, sharable) { this.encrypt(encryptionKey, it.toByteArray()).encodeBase64() }.toByteArray()
    }

    override fun fromBytes(context: Context, bytes: ByteArray, protocolVersion: Int, encryptionKey: String?): DataHolder {
        if (protocolVersion != this.writingProtocolVersion()) throw IncompatibleVersionException()
        return deserialize(context, bytes) { String(this.decrypt(encryptionKey, it.decodeBase64())) }
    }

    override fun writingProtocolVersion(): Int = 1

}