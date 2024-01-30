package dev.naijun.kakaotalk.emoticon

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import kotlin.experimental.xor

// scon decrypt

private fun preprocessKey(key: String): Triple<Int, Int, Int> {
    val keyBytes = key.toByteArray(StandardCharsets.UTF_8).let {
        it + it.copyOfRange(0, 32 - it.size)
    }

    fun computeKey(initial: Int, offset: Int) =
        (0 until 4).fold(initial) { acc, i ->
            (acc shl 8) or (keyBytes[i + offset].toInt() and 0xFF)
        }.let { if (it == 0) initial else it }

    return Triple(
        computeKey(301989938, 0),
        computeKey(623357073, 4),
        computeKey(-2004086252, 8)
    )
}

private fun processXor(key: String, source: ByteArray): ByteArray {
    var (key1, key2, key3) = preprocessKey(key)

    val result = ByteArray(source.size)
    for (i in source.indices) {
        val b = source[i]
        var b13 = 0
        var i16 = 0
        var i17 = 1

        for (j in 0 until 8) {
            if (key1 and 1 != 0) {
                key1 = ((-2147483550 xor key1) ushr 1) or Int.MIN_VALUE
                if (key2 and 1 != 0) {
                    key2 = ((key2 xor 1073741856) ushr 1) or -1073741824
                    i17 = 1
                } else {
                    key2 = (key2 ushr 1) and 1073741823
                    i17 = 0
                }
            } else {
                key1 = (key1 ushr 1) and Int.MAX_VALUE
                if (key3 and 1 != 0) {
                    key3 = ((key3 xor 268435458) ushr 1) or -268435456
                    i16 = 1
                } else {
                    key3 = (key3 ushr 1) and 268435455
                    i16 = 0
                }
            }
            b13 = (b13 shl 1) or (i17 xor i16)
        }

        result[i] = b xor b13.toByte()
    }

    return result
}

fun emoticonDecrypt(source: ByteArray): ByteArray {
    val key = "a271730728cbe141e47fd9d677e9006d"

    val inputStream = source.inputStream()
    val outputStream = ByteArrayOutputStream()

    var buffer = ByteArray(128)
    var bufferIndex = 0

    while (true) {
        try {
            val r11 = inputStream.read(buffer)

            if (r11 <= 0) break

            if (bufferIndex < 128) {
                buffer = processXor(key, buffer)
            }

            outputStream.write(buffer, 0, r11)
            bufferIndex += r11
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inputStream.close()
    outputStream.close()

    return outputStream.toByteArray()
}