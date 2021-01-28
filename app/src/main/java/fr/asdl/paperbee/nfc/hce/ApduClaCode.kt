package fr.asdl.paperbee.nfc.hce

enum class ApduClaCode(private val code: Int) {
    DEFAULT(0x00);
    val value get() = code.toByte()

    companion object {
        fun fromApduByte(byte: Byte): ApduClaCode? {
            for (i in values()) if (i.code == byte.toInt()) return i
            return null
        }
    }
}