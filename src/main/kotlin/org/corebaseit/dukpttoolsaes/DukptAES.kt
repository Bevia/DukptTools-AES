package org.corebaseit.dukpttoolsaes

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object DukptAES {

    fun deriveIPEK(bdk: ByteArray, ksn: ByteArray): ByteArray {
        // Derive IPEK using AES-CMAC
        val diversificationData = ksn.copyOfRange(0, 8) // first 8 bytes for diversification
        return aesCmac(bdk, diversificationData)
    }

    private fun aesCmac(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("AESCMAC", "BC")
        val keySpec = SecretKeySpec(key, "AES")
        mac.init(keySpec)
        return mac.doFinal(data)
    }
}
