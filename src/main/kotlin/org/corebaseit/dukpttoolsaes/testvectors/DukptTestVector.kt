package org.corebaseit.dukpttoolsaes.testvectors

data class DukptTestVector(
    val bdk: String,
    val ksn: String,
    val expectedIpek: String,
    val pin: String,
    val pan: String,
    val expectedEncryptedPin: String
)
