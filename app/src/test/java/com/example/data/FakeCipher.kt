package com.example.data

/** Cifrador de prueba: invierte y marca el texto para que nunca quede legible tal cual. */
class FakeCipher : TextCipher {
    override fun encrypt(plain: String): String = "FAKE:" + plain.reversed()
    override fun decrypt(encoded: String): String = encoded.removePrefix("FAKE:").reversed()
}
