package org.corebaseit.dukpttoolsaes

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesTerminalSimulator(
    private val ipek: ByteArray,
    private var ksn: ByteArray,
    var testMode: Boolean = false
) {

    fun encryptPin(
        pin: String,
        mode: AesMode,
        pan: String = "",
        ivOverride: ByteArray? = null
    ): Triple<ByteArray, ByteArray, ByteArray?> {
        val sessionKey = deriveSessionKey(ipek, ksn)
        val iv = ivOverride ?: if (testMode) ByteArray(16) { 0 } else ByteArray(16).apply {
            SecureRandom().nextBytes(this)
        }

        val pinBlock = generatePinBlock(pin)

        return when (mode) {
            AesMode.AES_128_ECB -> {
                val encrypted = aesEncryptEcb(sessionKey, pinBlock)
                Triple(encrypted, ksn, null)
            }
            AesMode.AES_256_CBC -> {
                val encrypted = aesEncryptCbc(sessionKey, iv, pinBlock)
                Triple(encrypted, ksn, iv)
            }
        }
    }

    private fun deriveSessionKey(ipek: ByteArray, ksn: ByteArray): ByteArray {
        return DukptAES.deriveIPEK(ipek, ksn)
    }

    private fun generatePinBlock(pin: String): ByteArray {
        val block = ByteArray(16)
        block[0] = 0x04
        block[1] = pin.length.toByte()
        for (i in 2 until 14) {
            block[i] = if (i - 2 < pin.length) pin[i - 2].digitToInt().toByte() else 0x0F
        }

        val padding = if (testMode) listOf(0x00, 0x00) else List(2) { SecureRandom().nextInt(256) }

        block[14] = padding[0].toByte()
        block[15] = padding[1].toByte()

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
