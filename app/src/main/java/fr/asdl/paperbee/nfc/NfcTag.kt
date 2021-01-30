package fr.asdl.paperbee.nfc

import android.content.Intent
import android.nfc.*
import android.nfc.tech.Ndef
import java.io.IOException

/**
 * Represents a Nfc Tag on the phone.
 * Check before instantiation that the action of the given [Intent] is
 * [NfcAdapter.ACTION_NDEF_DISCOVERED]
 */
class NfcTag @Throws(UnsupportedOperationException::class) constructor(intent: Intent) {

    private val receivedMessages: List<NdefMessage> =
        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.toList()
            ?.filterIsInstance<NdefMessage>() ?: listOf()
    private val ndef: Ndef

    init {
        val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            ?: throw UnsupportedOperationException("Device doesn't support Nfc")
        val tagTechs = listOf(*tag.techList)
        when {
            tagTechs.contains(Ndef::class.java.canonicalName) -> ndef = Ndef.get(tag)
            else -> throw UnsupportedOperationException("Fake tag used. Can only read PaperBee application tags.")
        }
    }

    @Throws(IOException::class, FormatException::class, TagLostException::class)
    fun writeData(message: NdefMessage): Boolean {
        ndef.connect()
        if (ndef.isConnected) {
            ndef.writeNdefMessage(message)
            return true
        }
        return false
    }

    fun readData(): List<NdefMessage> = receivedMessages

    @Throws(IOException::class)
    fun close() {
        ndef.close()
    }

}