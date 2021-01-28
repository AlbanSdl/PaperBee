package fr.asdl.paperbee.activities.fragments.sharing

import android.content.Intent
import android.nfc.NdefMessage
import fr.asdl.paperbee.PaperBeeApplication
import fr.asdl.paperbee.nfc.hce.ndef.NdefCard

class NfcShareService : NdefCard() {

    companion object {
        const val RESULT_CODE_READING = 0x100
        const val RESULT_CODE_READ_FINISHED = 0x101
        const val RESULT_CODE_ERROR = 0x200
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ndef = (this.application as PaperBeeApplication).nfcServiceListenerFrom?.invoke()
        if (ndef != null) this.cc.addNdefMessage(ndef, false)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onRead(message: NdefMessage) {
        (this.application as PaperBeeApplication).nfcServiceListenerTo?.invoke(RESULT_CODE_READING)
    }

    override fun onWrite(message: NdefMessage) {
        (this.application as PaperBeeApplication).nfcServiceListenerTo?.invoke(
            RESULT_CODE_READ_FINISHED)
        this.stopSelf()
    }

    override fun getTagId(): ByteArray {
        return byteArrayOf(0xF7.toByte(), 0x14, 0x28, 0x26, 0x02, 0x80.toByte(), 0x01)
    }

    override fun onDeactivated(reason: Int) {
        super.onDeactivated(reason)
        (this.application as PaperBeeApplication).nfcServiceListenerTo?.invoke(RESULT_CODE_ERROR)
    }

}