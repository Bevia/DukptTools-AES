package org.corebaseit.dukpttoolsaes.utils

object HexUtils {
    fun hexToBytes(hex: String): ByteArray {
        check(hex.length % 2 == 0) { "Hex string must have an even length" }
        return hex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
}