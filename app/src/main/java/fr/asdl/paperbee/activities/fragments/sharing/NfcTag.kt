package fr.asdl.paperbee.activities.fragments.sharing

import android.content.Intent
import android.nfc.*
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.io.IOException

/**
 * Represents a Nfc Tag on the phone.
 * Check before instantiation that the action of the given [Intent] is
 * [NfcAdapter.ACTION_TAG_DISCOVERED], [NfcAdapter.ACTION_TECH_DISCOVERED] or
 * [NfcAdapter.ACTION_NDEF_DISCOVERED]
 */
class NfcTag @Throws(UnsupportedOperationException::class) constructor(intent: Intent) {
    private val ndefTech = Ndef::class.java.canonicalName
    private val ndefTechFormatable = NdefFormatable::class.java.canonicalName

    private val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        ?: throw UnsupportedOperationException("Device doesn't support Nfc")
    private val receivedMessages: List<NdefMessage> =
        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.toList()
            ?.filterIsInstance<NdefMessage>() ?: listOf()
    private val ndef: Ndef?
    private val ndefFormatable: NdefFormatable?

    init {
        val tagTechs = listOf(*tag.techList)
        when {
            tagTechs.contains(ndefTech) -> {
                ndef = Ndef.get(tag)
                ndefFormatable = null
            }
            tagTechs.contains(ndefTechFormatable) -> {
                ndefFormatable = NdefFormatable.get(tag)
                ndef = null
            }
            else -> {
                throw UnsupportedOperationException("Tag doesn't support ndef")
            }
        }
    }

    @Throws(IOException::class, FormatException::class, TagLostException::class)
    fun writeData(message: NdefMessage): Boolean {
        if (ndef != null) {
            ndef.connect()
            if (ndef.isConnected) {
                ndef.writeNdefMessage(message)
                return true
            }
        } else if (ndefFormatable != null) {
            ndefFormatable.connect()
            if (ndefFormatable.isConnected) {
                ndefFormatable.format(message)
                return true
            }
        }
        return false
    }

    fun readData(): List<NdefMessage> = receivedMessages

    @Throws(IOException::class)
    fun close() {
        ndef?.close() ?: ndefFormatable?.close()
    }

}