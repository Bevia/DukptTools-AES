package org.corebaseit.dukpttoolsaes

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.corebaseit.dukpttoolsaes.testvectors.DukptTestVectors
import java.security.Security

class HSMClient {

    fun main() {
        Security.addProvider(BouncyCastleProvider())
        val simulator = DukptSimulator()
        simulator.runSimulation(
            pin = "1234",
            pan = "4532111122223333",
            mode = AesMode.AES_128_ECB // or AES_256_CBC
        )

        val allPassed = DukptTestVectors.validateAll()
        println(if (allPassed) "✅ All tests passed!" else "❌ Some tests failed.")

    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            HSMClient().main()
        }
    }
}
