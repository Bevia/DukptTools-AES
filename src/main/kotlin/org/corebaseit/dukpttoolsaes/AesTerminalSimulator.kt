package org.corebaseit.dukpttoolsaes

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesTerminalSimulator(private val ipek: ByteArray, private var ksn: ByteArray,  var testMode: Boolean = false) {

    fun encryptPin(
        pin: String,
        mode: AesMode,
        pan: String = "",
        ivOverride: ByteArray? = null
    ): Triple<ByteArray, ByteArray, ByteArray?> {
        val sessionKey = deriveSessionKey(ipek, ksn)
        val iv = if (testMode) ByteArray(16) { 0 } else ByteArray(16).apply { SecureRandom().nextBytes(this) }

        return when (mode) {
            AesMode.AES_128_ECB -> {
                val pinBlock = generatePinBlockEcb(pin, pan)
                val encrypted = aesEncryptEcb(sessionKey, pinBlock)
                Triple(encrypted, ksn, null)
            }
            AesMode.AES_256_CBC -> {
                val pinBlock = generatePinBlockCbc(pin)
                val iv = ivOverride ?: ByteArray(16).apply { SecureRandom().nextBytes(this) }
                val encrypted = aesEncryptCbc(sessionKey, iv, pinBlock)
                Triple(encrypted, ksn, iv)
            }
        }
    }


    private fun deriveSessionKey(ipek: ByteArray, ksn: ByteArray): ByteArray {
        return DukptAES.deriveIPEK(ipek, ksn)
    }

    private fun generatePinBlockEcb(pin: String, pan: String): ByteArray {
        val block = ByteArray(16)
        block[0] = 0x04
        block[1] = pin.length.toByte()
        for (i in 2 until 14) {
            block[i] = if (i - 2 < pin.length) pin[i - 2].digitToInt().toByte() else 0x0F
        }
        if (testMode) {
            block[14] = 0x00
            block[15] = 0x00
        } else {
            val random = SecureRandom()
            block[14] = random.nextInt(256).toByte()
            block[15] = random.nextInt(256).toByte()
        }

        return block
    }

    private fun generatePinBlockCbc(pin: String): ByteArray {
        val block = ByteArray(16)
        block[0] = 0x04
        block[1] = pin.length.toByte()
        for (i in 2 until 14) {
            block[i] = if (i - 2 < pin.length) pin[i - 2].digitToInt().toByte() else 0x0F
        }
        if (testMode) {
            block[14] = 0x00
            block[15] = 0x00
        } else {
            val random = SecureRandom()
            block[14] = random.nextInt(256).toByte()
            block[15] = random.nextInt(256).toByte()
        }
        return block
    }


    private fun aesEncryptEcb(key: ByteArray, data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/ECB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"))
        return cipher.doFinal(data)
    }

    private fun aesEncryptCbc(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        return cipher.doFinal(data)
    }
}
