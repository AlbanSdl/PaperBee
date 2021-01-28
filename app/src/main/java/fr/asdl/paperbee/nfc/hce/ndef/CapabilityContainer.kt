package fr.asdl.paperbee.nfc.hce.ndef

import android.nfc.NdefMessage

class CapabilityContainer {

    private val mappingVersion = 0x20
    private val maxRApduLength = 0xFFFF
    private val maxCApduLength = 0xFFFF
    private val files: ArrayList<TLVBlock> = arrayListOf()

    val fileCount get() = files.size

    /**
     * Adds a [NdefMessage] into the [CapabilityContainer].
     */
    fun addNdefMessage(message: NdefMessage, readOnly: Boolean): TLVBlock {
        var chosenId = -1
        for (i in 0x0001..0xFFFE) {
            if (i != 0xE102 && i != 0xE103 && i != 0x3F00 && i != 0x3FFF && files.find { it.id == i } == null) {
                chosenId = i
                break
            }
        }
        if (chosenId < 0) throw IndexOutOfBoundsException("Unable to allocate id for Ndef message !")
        val block = TLVBlock(
            chosenId,
            TLVBlock.Type.NDEF,
            if (readOnly) TLVBlock.FileAccess.READ_ONLY else TLVBlock.FileAccess.READ_WRITE,
            message
        )
        files.add(block)
        return block
    }

    /**
     * Removes a [NdefMessage] from the [CapabilityContainer] index.
     */
    fun removeNdefMessage(id: Int) {
        this.files.remove(this.getNdefMessage(id))
    }

    /**
     * Retrieves a [NdefMessage] from the [CapabilityContainer].
     */
    fun getNdefMessage(id: Int): TLVBlock? {
        return this.files.find { it.id == id }
    }

    fun toByteArray(): ByteArray {
        var bArray = byteArrayOf(
            mappingVersion.toByte(),
            *toByteArray(maxRApduLength),
            *toByteArray(maxCApduLength)
        )
        for (i in files) bArray = mergeBytes(i.getCCByteArray(), bArray)
        val rbArray = ByteArray(bArray.size + 2)
        System.arraycopy(toByteArray(rbArray.size), 0, rbArray, 0, 2)
        System.arraycopy(bArray, 0, rbArray, 2, bArray.size)
        return rbArray
    }

    companion object {
        private fun toByteArray(value: Int, expectedLength: Int = 2): ByteArray {
            val array = ByteArray(expectedLength)
            for (i in 0 until expectedLength)
                array[i] = (value shr i * 8 % 256).toByte()
            array.reverse()
            return array
        }

        private fun mergeBytes(append: ByteArray, into: ByteArray): ByteArray {
            val b = ByteArray(append.size + into.size)
            System.arraycopy(into, 0, append, 0, into.size)
            System.arraycopy(append, 0, append, into.size, append.size)
            return b
        }
    }
}