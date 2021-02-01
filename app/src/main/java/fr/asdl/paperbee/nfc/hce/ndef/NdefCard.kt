package fr.asdl.paperbee.nfc.hce.ndef

import android.nfc.FormatException
import android.nfc.NdefMessage
import androidx.annotation.CallSuper
import fr.asdl.paperbee.exceptions.NfcSelectionException
import kotlin.math.min

abstract class NdefCard : NdefCardEmulator() {

    val cc = CapabilityContainer()
    private var selection: TLVBlock? = null
    private var ccSelected: Boolean = false
    private var pending: Boolean = false

    final override fun selectCapabilityContainer(): Boolean {
        ccSelected = true
        if (selection != null && pending) {
            selection!!.rawData = selection!!.content.toByteArray()
            pending = false
        }
        selection = null
        return cc.fileCount > 0
    }

    final override fun selectFile(id: Int): Boolean {
        ccSelected = false
        if (id != selection?.id && pending) {
            selection!!.rawData = selection!!.content.toByteArray()
            pending = false
        }
        selection = cc.getNdefMessage(id)
        return selection != null
    }

    final override fun read(offset: Int, length: Int): ByteArray {
        fun ofRange(byteArray: ByteArray): ByteArray =
            byteArray.copyOfRange(min(offset, byteArray.size - 1), min(offset + length, byteArray.size))
        if (selection == null && !ccSelected) throw NfcSelectionException()
        if (ccSelected) return ofRange(cc.toByteArray())
        this.onRead(selection!!.content)
        return ofRange(selection!!.toByteArray())
    }

    final override fun write(offset: Int, data: ByteArray?) {
        ccSelected = false
        if (data == null) return
        if (selection == null) throw NfcSelectionException()
        if (this.selection!!.rawData.size < offset + data.size) {
            val prev = this.selection!!.rawData
            this.selection!!.rawData = ByteArray(offset + data.size)
            System.arraycopy(prev, 0, this.selection!!.rawData, 0, offset)
        }
        System.arraycopy(data, 0, this.selection!!.rawData, offset, data.size)
        try {
            val message = NdefMessage(this.selection!!.rawData)
            this.selection!!.content = message
            pending = false
            this.onWrite(message)
        } catch (format: FormatException) {
            pending = true // another request should complete the NdefMessage
        }
    }

    @CallSuper
    override fun onDeactivated(reason: Int) {
        selection = null
        ccSelected = false
    }

    protected abstract fun onRead(message: NdefMessage)
    protected abstract fun onWrite(message: NdefMessage)

}