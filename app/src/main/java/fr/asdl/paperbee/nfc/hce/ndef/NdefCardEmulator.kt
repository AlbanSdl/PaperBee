package fr.asdl.paperbee.nfc.hce.ndef

import fr.asdl.paperbee.exceptions.NfcSelectionException
import fr.asdl.paperbee.nfc.hce.ApduClaCode
import fr.asdl.paperbee.nfc.hce.ApduInsCode
import fr.asdl.paperbee.nfc.hce.ApduResponseCode
import fr.asdl.paperbee.nfc.hce.BaseNfcCardEmulationService
import kotlin.jvm.Throws

abstract class NdefCardEmulator : BaseNfcCardEmulationService() {

    /**
     * Called from the main thread when the nfc capable device (reader) is requesting access
     * to the Capability Container.
     * @return whether the reader can access the CC
     */
    protected abstract fun selectCapabilityContainer(): Boolean

    /**
     * Called from the main thread when the nfc capable device (reader) is requesting access
     * to the file of the given id. Capability container is filtered out and will call
     * [selectCapabilityContainer] instead.
     * @return whether the reader can access to the file of the given id (ie. whether it exists and
     * is visible)
     */
    protected abstract fun selectFile(id: Int): Boolean

    /**
     * Called from the main thread when the nfc reader is attempting to read the selected file.
     * If this method is called without a selected file, throw a [NfcSelectionException]
     * @return the raw data of the file with the given [offset] and of the given [length].
     * Can return null if the file is not available at this point. In this case, call [sendResponseApdu]
     * with the data once it's available.
     */
    @Throws(NfcSelectionException::class)
    protected abstract fun read(offset: Int, length: Int): ByteArray?

    /**
     * Called from the main thread when the nfc reader attempts to write to the selected file.
     * If this method is called without a selected file, throw a [NfcSelectionException].
     */
    @Throws(NfcSelectionException::class)
    protected abstract fun write(offset: Int, data: ByteArray?)

    /**
     * Returns the Application Id (AID) of the tag.
     * The first byte of the id indicates how it has been registered:
     *      0xA means the aid has been registered internationally
     *      0xD means the aid has been registered nationally
     *      0xF means the aid is a proprietary id and is not supposed to be registered
     * An aid is supposed to have an even length, containing up to 16 bytes.
     * This is the aid you set in the <host-apdu-service> xml file.
     */
    protected abstract fun getTagId(): ByteArray

    final override fun onReaderDetected(
        cla: ApduClaCode,
        insCode: ApduInsCode,
        p1Code: Int,
        p2Code: Int,
        content: ByteArray?,
        expectedLength: Int?
    ): ByteArray? {
        if (cla != ApduClaCode.DEFAULT) return getFormattedResponse(ApduResponseCode.ERROR_CLA)
        if (insCode == ApduInsCode.SELECT && p1Code == 0x04 && p2Code == 0x00) {
            return getFormattedResponse(
                if (content.contentEquals(getTagId())) ApduResponseCode.SUCCESS
                else ApduResponseCode.ERROR_NO_SUCH_ELEMENT
            )
        }
        if (insCode == ApduInsCode.SELECT && p1Code == 0x00 && p2Code == 0x0C && expectedLength == null) {
            val accessAllowed = content?.size == 2 && if (content.contentEquals(byteArrayOf(0xE1.toByte(), 0x03)))
                selectCapabilityContainer() else selectFile(content[0].toInt() + content[1].toInt())
            return getFormattedResponse(
                if (accessAllowed) ApduResponseCode.SUCCESS
                else ApduResponseCode.ERROR_NO_SUCH_ELEMENT
            )
        }
        if (insCode == ApduInsCode.READ_BINARY && expectedLength != null) {
            return try {
                val d = read(p1Code + p2Code, expectedLength)
                if (d != null) getFormattedResponse(ApduResponseCode.SUCCESS, d)
                else null
            } catch (nfcSelection: NfcSelectionException) {
                getFormattedResponse(ApduResponseCode.ERROR_NO_SUCH_ELEMENT)
            }
        }
        if (insCode == ApduInsCode.UPDATE_BINARY && expectedLength == null) {
            return try {
                write(p1Code + p2Code, content)
                getFormattedResponse(ApduResponseCode.SUCCESS)
            } catch (nfcSelection: NfcSelectionException) {
                getFormattedResponse(ApduResponseCode.ERROR_NO_SUCH_ELEMENT)
            }
        }

        // Command not understood
        return ApduResponseCode.ERROR_GENERIC.value
    }

}