package fr.asdl.paperbee.nfc.hce.ndef

import android.nfc.NdefMessage

class TLVBlock(val id: Int, var type: Type, var access: FileAccess, var content: NdefMessage) {

    var rawData: ByteArray = content.toByteArray()

    enum class Type(val code: Byte) {
        NDEF(0x04), PROPRIETARY(0x05)
    }

    enum class FileAccess(val readCode: Byte, val writeCode: Byte) {
        READ_ONLY(0x00, 0xFF.toByte()),
        READ_WRITE(0x00, 0x00)
    }

    internal fun getCCByteArray(): ByteArray {
        return byteArrayOf(
            this.type.code,
            0x06,
            *toByteArray(id),
            *toByteArray(0xFFFE),
            access.readCode,
            access.writeCode
        )
    }

    fun toByteArray(): ByteArray {
        val bNdef = content.toByteArray()
        return byteArrayOf(*Companion.toByteArray(bNdef.size), *bNdef)
    }

    companion object {
        private fun toByteArray(value: Int, expectedLength: Int = 2): ByteArray {
            val array = ByteArray(expectedLength)
            for (i in 0 until expectedLength)
                array[i] = (value shr i * 8 % 256).toByte()
            array.reverse()
            return array
        }
    }

}