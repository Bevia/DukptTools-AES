package org.corebaseit.dukpttoolsaes

import org.corebaseit.dukpttoolsaes.utils.HexUtils

class DukptSimulator {

    private fun isHex(input: String): Boolean = input.matches(Regex("^[0-9A-Fa-f]{16}$"))

    fun runSimulation(
        pin: String = "1234",
        pan: String = "4532111122223333",
        mode: AesMode = AesMode.AES_256_CBC // default to CBC, optionally configurable
    ) {
        try {
            val validatedPin = pin.also { validatePin(it) }
            val validatedPan = pan.also { validatePan(it) }

            printSimulationHeader(validatedPin, validatedPan)

            println("DUKPT Format ID: 4 (AES-DUKPT)")
            val bdk = generateBdk(mode)
            println("BDK (${mode.name}): ${HexUtils.bytesToHex(bdk)}")

            val ksn = generateInitialKsn()
            println("Initial KSN (Format 4): ${HexUtils.bytesToHex(ksn)}")

            val ipek = DukptAES.deriveIPEK(bdk, ksn)
            println("IPEK (Derived using AES): ${HexUtils.bytesToHex(ipek)}")
            println()

            simulateTransaction(mode, validatedPin, validatedPan, ipek, ksn, bdk)

        } catch (e: IllegalArgumentException) {
            System.err.println("Validation Error: ${e.message}")
        } catch (e: Exception) {
            System.err.println("Simulation Error: ${e.message}")
        }
    }


    private fun validatePin(pin: String) {
        if (isHex(pin)) return
        require(pin.length in 4..12) { "PIN must be between 4 and 12 digits" }
        require(pin.all { it.isDigit() }) { "PIN must contain only digits" }
    }

    private fun validatePan(pan: String) {
        require(pan.length >= 13) { "PAN must be at least 13 digits" }
        require(pan.all { it.isDigit() }) { "PAN must contain only digits" }
    }

    private fun printSimulationHeader(pin: String, pan: String) {
        println("Simulating AES-DUKPT PIN encryption/decryption")
        println("==============================================")
        println("PIN: ${maskPin(pin)}")
        println("PAN: ${maskPan(pan)}")
        println()
    }

    private fun generateBdk(mode: AesMode): ByteArray {
        return when (mode) {
            AesMode.AES_128_ECB -> HexUtils.hexToBytes("00112233445566778899AABBCCDDEEFF")
            AesMode.AES_256_CBC -> HexUtils.hexToBytes("00112233445566778899AABBCCDDEEFF0123456789ABCDEFFEDCBA9876543210")
        }
    }


    private fun generateInitialKsn(): ByteArray {
        return HexUtils.hexToBytes("FFFF9876543210E00000000000000000") // 16-byte Format 4 KSN
    }

    private fun simulateTransaction(
        mode: AesMode,
        pin: String,
        pan: String,
        ipek: ByteArray,
        ksn: ByteArray,
        bdk: ByteArray
    ) {
        val terminal = AesTerminalSimulator(ipek, ksn)
        val hsm = AesHsmSimulator(bdk)

        println("Terminal encrypting PIN using ${mode.name}...")

        val (encryptedPin, currentKsn, iv) = when (mode) {
            AesMode.AES_128_ECB -> {
                val result = terminal.encryptPin(pin, mode, pan)
                Triple(result.first, result.second, null)
            }
            AesMode.AES_256_CBC -> terminal.encryptPin(pin, mode)
        }

        println("Encrypted PIN block: ${HexUtils.bytesToHex(encryptedPin)}")
        println("Current KSN: ${HexUtils.bytesToHex(currentKsn)}")
        iv?.let { println("IV used for CBC: ${HexUtils.bytesToHex(it)}") }
        println()

        println("HSM decrypting PIN using ${mode.name}...")

        val decryptedPin = when (mode) {
            AesMode.AES_128_ECB -> hsm.decryptPin(
                mode = mode,
                encrypted = encryptedPin,
                ksn = currentKsn,
                pan = pan
            )
            AesMode.AES_256_CBC -> hsm.decryptPin(
                mode = mode,
                encrypted = encryptedPin,
                ksn = currentKsn,
                iv = iv ?: throw IllegalStateException("IV must not be null for CBC mode")
            )
        }

        println("Decrypted PIN (unmasked): $decryptedPin")

        if (pin != decryptedPin) {
            throw IllegalStateException("PIN verification failed!")
        }
    }



    private fun maskPin(pin: String): String = MASK_CHARACTER.repeat(pin.length)

    private fun maskPan(pan: String): String {
        return when {
            pan.length <= 4 -> pan
            else -> "${pan.take(6)}${"*".repeat(pan.length - 10)}${pan.takeLast(4)}"
        }
    }

    companion object {
        private const val MASK_CHARACTER = "*"
    }
}
