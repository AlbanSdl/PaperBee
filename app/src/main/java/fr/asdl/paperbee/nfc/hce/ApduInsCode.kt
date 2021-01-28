package fr.asdl.paperbee.nfc.hce

enum class ApduInsCode(private val code: Int) {
    SELECT(0xA4),
    READ_BINARY(0xB0),
    UPDATE_BINARY(0xD6);
    val value get() = code.toByte()

    companion object {
        fun fromApduByte(byte: Byte): ApduInsCode? {
            for (i in values()) if (i.code == byte.toInt()) return i
            return null
        }
    }
}