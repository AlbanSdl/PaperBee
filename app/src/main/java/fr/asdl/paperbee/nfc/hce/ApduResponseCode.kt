package fr.asdl.paperbee.nfc.hce

enum class ApduResponseCode(private val code: Int) {
    SUCCESS(0x9000),
    ERROR_NO_SUCH_ELEMENT(0x6A82),
    ERROR_GENERIC(0x6F00),
    ERROR_CLA(0x6E00),
    ERROR_INS(0x6D00),
    ERROR_P1_P2(0x6A86);

    val value: ByteArray get() {
        val array = ByteArray(2)
        for (i in 0 until 2)
            array[i] = (this.code shr i * 8 % 256).toByte()
        return array
    }
}