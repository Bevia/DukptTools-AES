package org.corebaseit.dukpttoolsaes

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesHsmSimulator(private val bdk: ByteArray) {

    fun decryptPin(
        mode: AesMode,
        encrypted: ByteArray,
        ksn: ByteArray,
        pan: String = "",
        iv: ByteArray = ByteArray(16) // default IV placeholder
    ): String {
        val ipek = DukptAES.deriveIPEK(bdk, ksn)
        val sessionKey = DukptAES.deriveIPEK(ipek, ksn)

        return when (mode) {
            AesMode.AES_128_ECB -> {
                val decrypted = aesDecryptEcb(sessionKey, encrypted)
                extractPinFromBlockEcb(decrypted, pan)
            }
            AesMode.AES_256_CBC -> {
                val decrypted = aesDecryptCbc(sessionKey, iv, encrypted)
                extractPinFromBlockCbc(decrypted)
            }
        }
    }

    private fun aesDecryptEcb(key: ByteArray, encrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/ECB/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))
        return cipher.doFinal(encrypted)
    }

    private fun aesDecryptCbc(key: ByteArray, iv: ByteArray, encrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        return cipher.doFinal(encrypted)
    }

    private fun extractPinFromBlockEcb(pinBlock: ByteArray, pan: String): String {
        return "1234" // Replace with your actual ECB block parser
    }

    private fun extractPinFromBlockCbc(pinBlock: ByteArray): String {
        val pinLength = pinBlock[1].toInt()
        val pinDigits = pinBlock.slice(2 until (2 + pinLength))
        return pinDigits.joinToString("") { it.toString() }
    }
}
