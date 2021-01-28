package fr.asdl.paperbee.nfc.hce

import android.nfc.cardemulation.HostApduService
import android.os.Bundle

abstract class BaseNfcCardEmulationService : HostApduService() {

    /**
     * Called when receiving an APDU command from a remote device.
     * The [BaseNfcCardEmulationService] parses nfc headers and calls [onReaderDetected]
     * if they are properly set (and understood)
     */
    final override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray? {
        if (commandApdu == null || commandApdu.size < 4)
            return ApduResponseCode.ERROR_GENERIC.value
        return try {
            val lc = if (commandApdu.size > 4) commandApdu[4].toInt() else null
            val content = if (lc != null && commandApdu.size > 4 + lc) ByteArray(lc) else null
            if (content != null) System.arraycopy(commandApdu, 5, content, 0, content.size)
            this.onReaderDetected(
                ApduClaCode.fromApduByte(commandApdu[0]) ?: return ApduResponseCode.ERROR_CLA.value,
                ApduInsCode.fromApduByte(commandApdu[1]) ?: return ApduResponseCode.ERROR_INS.value,
                commandApdu[2].toInt(),
                commandApdu[3].toInt(),
                content,
                if (content != null && commandApdu.size > 5 + content.size) commandApdu[5 + content.size].toInt()
                else if (content == null && lc != null) lc
                else null
            )
        } catch (e: IndexOutOfBoundsException) {
            ApduResponseCode.ERROR_GENERIC.value
        }
    }

    /**
     * Called when receiving an understandable APDU (Application Protocol Data Unit)
     * from a remote device.
     * @return a byte-array containing the response APDU, or null if no
     *         response APDU can be sent at this point. In order to create this byte array
     *         it's recommended to use [getFormattedResponse] if the response has a body, the
     *         [ApduResponseCode.value] otherwise.
     */
    protected abstract fun onReaderDetected(
        cla: ApduClaCode,
        insCode: ApduInsCode,
        p1Code: Int,
        p2Code: Int,
        content: ByteArray?,
        expectedLength: Int?
    ): ByteArray?

    /**
     * If sending a response, use this method to get the proper ByteArray to send to Reader
     */
    protected fun getFormattedResponse(responseCode: ApduResponseCode, data: ByteArray? = null): ByteArray {
        if (data == null) return responseCode.value
        val b = ByteArray(data.size + 2)
        System.arraycopy(data, 0, b, 0, data.size)
        System.arraycopy(responseCode.value, 0, b, data.size, 2)
        return b
    }

}