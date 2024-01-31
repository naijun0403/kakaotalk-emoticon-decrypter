import dev.naijun.kakaotalk.emoticon.emoticonDecrypt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File

suspend fun main() {
    CoroutineScope(Dispatchers.IO).run {
        val clazz = this::class.java

        val encryptedBuffer = clazz.getResourceAsStream("/4441957.emot_002_x3.webp")
            ?.readAllBytes() ?: throw IllegalStateException("Cannot read emoticon file")

        val decryptedBuffer = emoticonDecrypt(encryptedBuffer)

        val decryptedFile = File("src/test/resources/4441957.emot_002_x3.decrypted.webp")
        decryptedFile.writeBytes(decryptedBuffer)

        println("Done")
    }
}