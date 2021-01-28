package fr.asdl.paperbee.nfc.hce.ndef

import android.nfc.FormatException
import android.nfc.NdefMessage
import androidx.annotation.CallSuper
import fr.asdl.paperbee.exceptions.NfcSelectionException

abstract class NdefCard : NdefCardEmulator() {

    val cc = CapabilityContainer()
    private var selection: TLVBlock? = null
    private var ccSelected: Boolean = false

    final override fun selectCapabilityContainer(): Boolean {
        ccSelected = true
        return cc.fileCount > 0
    }

    final override fun selectFile(id: Int): Boolean {
        ccSelected = false
        selection = cc.getNdefMessage(id)
        return selection != null
    }

    final override fun read(offset: Int, length: Int): ByteArray? {
        if (selection == null && !ccSelected) throw NfcSelectionException()
        if (ccSelected) {
            ccSelected = false
            return cc.toByteArray()
        }
        this.onRead(selection!!.content)
        return selection!!.toByteArray()
    }

    final override fun write(offset: Int, data: ByteArray?) {
        ccSelected = false
        if (selection == null) throw NfcSelectionException()
        try {
            this.selection!!.content = NdefMessage(data)
            this.onWrite(this.selection!!.content)
        } catch (f: FormatException) {
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