package fr.asdl.paperbee.sharing

import fr.asdl.paperbee.exceptions.WrongPasswordException
import java.lang.Exception
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

abstract class SharingFactory<T> {

    private val encryptionAlgorithm = "AES"

    private fun getBytes(sharable: T): ByteArray {
        val bytes = this.toBytes(sharable)
        val valHeaderLength = ceil((bytes.size - 31).toFloat() / 255).toInt() + if ((bytes.size - 31) % 255 == 0) 1 else 0
        val byteArray = ByteArray(bytes.size + 1 + valHeaderLength)
        for (i in 0..valHeaderLength) {
            byteArray[i] = if (i == 0)
                fillByte(this.writingProtocolVersion(), 0, 0, min(31, bytes.size))
            else fillByte(max(0, min(255, bytes.size - 31 - 255 * (i - 1))))
        }
        System.arraycopy(bytes, 0, byteArray, valHeaderLength + 1, bytes.size)
        return byteArray
    }

    private fun getBytes(list: List<T>): ByteArray {
        val asBytes = list.map { this.getBytes(it) }
        val arr = ByteArray(asBytes.map { it.size }.sum())
        var indexStart = 0
        asBytes.forEach {
            System.arraycopy(it, 0, arr, indexStart, it.size)
            indexStart += it.size
        }
        return arr
    }

    private fun parseBytes(byteArray: ByteArray): List<T> {
        var cursor = 0
        val list: ArrayList<T> = arrayListOf()
        while (cursor < byteArray.size) {
            var length = 0
            val protocolVersion = getFromByte(byteArray[cursor], 0, 2)
            while (true) {
                val isStart = length == 0
                val n = getFromByte(byteArray[cursor++], if (isStart) 3 else 0, 7)
                length += n
                if (n == 0 || (n != 255 && !isStart) || (n != 31 && isStart)) break
            }
            val elemArray = ByteArray(length)
            System.arraycopy(byteArray, cursor, elemArray, 0, length)
            list.add(this.fromBytes(elemArray, protocolVersion))
            cursor += length
        }
        return list
    }

    private fun fillByte(vararg bits: Int): Byte {
        var i = 0
        for (v in bits.indices)
            i += bits[v] shl v
        return i.toByte()
    }

    private fun getFromByte(byte: Byte, from: Int, to: Int = from): Int {
        val b = byte.toInt()
        var i = 0
        for (v in from..to) i += (b and (1 shl v)) shr from
        return i
    }

    protected open fun runCipher(operationMode: Int, key: String, byteArray: ByteArray): ByteArray {
        val salt = ByteArray(16)
        for (i in 0 until 16) salt[i] = key.toCharArray()[i % key.length].toByte()
        val spec = PBEKeySpec(key.toCharArray(), salt, 1000, 256)
        val generated = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec)
        val generatedKey = SecretKeySpec(generated.encoded, encryptionAlgorithm)
        val cipher: Cipher = Cipher.getInstance(encryptionAlgorithm)
        cipher.init(operationMode, generatedKey)
        return cipher.doFinal(byteArray)
    }

    fun encrypt(encryptionKey: String?, values: List<T>): ByteArray {
        if (encryptionKey == null) return this.getBytes(values)
        return this.runCipher(Cipher.ENCRYPT_MODE, encryptionKey, this.getBytes(values))
    }

    fun decrypt(encryptionKey: String?, value: ByteArray): List<T> {
        try {
            if (encryptionKey == null) return this.parseBytes(value)
            return this.parseBytes(this.runCipher(Cipher.DECRYPT_MODE, encryptionKey, value))
        } catch (e: Exception) {
            throw WrongPasswordException()
        }
    }

    protected abstract fun toBytes(sharable: T): ByteArray
    protected abstract fun fromBytes(bytes: ByteArray, protocolVersion: Int): T
    abstract fun writingProtocolVersion(): Int

}