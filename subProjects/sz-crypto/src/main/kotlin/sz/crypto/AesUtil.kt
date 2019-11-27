package sz.crypto

import org.bouncycastle.util.encoders.Base64
import java.nio.charset.Charset
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

//
// Created by kk on 2019/11/27.
//
@Suppress("MemberVisibilityCanBePrivate")
object AesUtil {

    private const val algorithm = "AES"
    private const val cipherTransformation = "AES/ECB/PKCS5Padding"
    private const val keysize = 128                                 // 密钥长度 128 位, 32 字节

    private fun buildKey(pwd: String, charset: Charset = Charsets.UTF_8): SecretKeySpec {
        val kgen = KeyGenerator.getInstance(algorithm)
        val random = SecureRandom.getInstance("SHA1PRNG", "SUN")
        random.setSeed(pwd.toByteArray(charset))
        kgen.init(keysize, random)
        val secretKey = kgen.generateKey()
        val keyBytes = secretKey.encoded
        println("keyBytes size: ${keyBytes.size}")
        return SecretKeySpec(secretKey.encoded, algorithm)
    }

    fun encrypt(plainBytes: ByteArray, pwd: String, charset: Charset = Charsets.UTF_8): ByteArray {
        val cipher = Cipher.getInstance(cipherTransformation)
        val key = buildKey(pwd, charset)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(plainBytes)
    }

    fun encrypt(plainTxt: String, pwd: String, charset: Charset = Charsets.UTF_8): String {
        val encryptedBytes = encrypt(
            plainBytes = plainTxt.toByteArray(charset),
            pwd = pwd,
            charset = charset)
        return Base64.toBase64String(encryptedBytes)
    }

    fun decrypt(encryptedBytes: ByteArray, pwd: String, charset: Charset = Charsets.UTF_8): ByteArray {
        val cipher = Cipher.getInstance(cipherTransformation)
        val key = buildKey(pwd, charset)
        cipher.init(Cipher.DECRYPT_MODE, key)

        return cipher.doFinal(encryptedBytes)
    }

    fun decrypt(encryptedTxt: String, pwd: String, charset: Charset = Charsets.UTF_8): String {
        val encryptedBytes = Base64.decode(encryptedTxt)
        val plainBytes = decrypt(encryptedBytes, pwd, charset)
        return plainBytes.toString(charset)
    }
}