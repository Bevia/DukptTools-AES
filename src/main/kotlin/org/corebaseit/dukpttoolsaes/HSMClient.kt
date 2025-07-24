package org.corebaseit.dukpttoolsaes

import org.bouncycastle.jce.provider.BouncyCastleProvider
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
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            HSMClient().main()
        }
    }
}
