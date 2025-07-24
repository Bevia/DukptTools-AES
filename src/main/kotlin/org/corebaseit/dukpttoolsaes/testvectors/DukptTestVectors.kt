package org.corebaseit.dukpttoolsaes.testvectors

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.corebaseit.dukpttoolsaes.AesMode
import org.corebaseit.dukpttoolsaes.AesTerminalSimulator
import org.corebaseit.dukpttoolsaes.DukptAES
import org.corebaseit.dukpttoolsaes.utils.HexUtils
import java.io.File

object DukptTestVectors {

    private val mapper = jacksonObjectMapper()

    val vector = DukptTestVector(
        bdk = "0123456789ABCDEFFEDCBA9876543210",
        ksn = "FFFF9876543210E00000000000000000",
        expectedIpek = "9E9A8F275E612386E3F22C10CEE59CE9",
        pin = "1234",
        pan = "4532111122223333",
        expectedEncryptedPin = "94cad288c71da94aaf0b902ba30b67667db8541728901ccabf9a07e96944fbab"
    )

    private val vectors: List<DukptTestVector> by lazy {
        val file = File("src/main/resources/test-vectors.json")
        if (file.exists()) {
            mapper.readValue(file)
        } else {
            println("⚠️ JSON file not found, loading default vector")
            listOf(vector)
        }
    }

    fun validateAll(): Boolean {
        var allPassed = true

        for ((index, vector) in vectors.withIndex()) {
            println("🔎 Validating vector #${index + 1}...")

            val bdk = HexUtils.hexToBytes(vector.bdk)
            val ksn = HexUtils.hexToBytes(vector.ksn)
            val expectedIpek = HexUtils.hexToBytes(vector.expectedIpek)
            val expectedEncryptedPin = HexUtils.hexToBytes(vector.expectedEncryptedPin)

            val computedIpek = DukptAES.deriveIPEK(bdk, ksn)
            val ipekMatch = computedIpek.contentEquals(expectedIpek)

            if (!ipekMatch) {
                println("❌ IPEK mismatch!")
                println("Expected: ${vector.expectedIpek}")
                println("Actual:   ${HexUtils.bytesToHex(computedIpek)}")
            } else {
                println("✅ IPEK verified.")
            }

            // 🔐 Encrypt using a fixed IV for consistent output
            val terminal = AesTerminalSimulator(computedIpek, ksn, testMode = true)
            val fixedIV = ByteArray(16) { 0 } // all zeros — stable for tests
            val (encrypted, _, _) = terminal.encryptPin(
                pin = vector.pin,
                mode = AesMode.AES_256_CBC,
                ivOverride = fixedIV
            )

            val encryptionMatch = encrypted.contentEquals(expectedEncryptedPin)

            if (!encryptionMatch) {
                println("❌ Encrypted PIN mismatch!")
                println("Expected: ${vector.expectedEncryptedPin}")
                println("Actual:   ${HexUtils.bytesToHex(encrypted)}")
                allPassed = false // 🔥 This line ensures overall result reflects failure
            } else {
                println("✅ Encrypted PIN verified.")
            }
        }

        return allPassed
    }
}
