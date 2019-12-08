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

/**
 * @param keysize: 密钥长度, 可选值(128, 192, 256), 默认128
 * @param cipherTransformation the standard name of the requested key algorithm.
 * See the KeyGenerator section in the <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyGenerator">
 * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
 * for information about standard algorithm names.
 */
@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
class AesUtil(val keysize: Int = 128,
              val cipherTransformation: String = "AES/ECB/PKCS5Padding") {

    private val algorithm = "AES"

    private fun buildKey(pwd: String, charset: Charset = Charsets.UTF_8): SecretKeySpec {
        val kgen = KeyGenerator.getInstance(algorithm)
        val random = SecureRandom.getInstance("SHA1PRNG", "SUN")
        random.setSeed(pwd.toByteArray(charset))
        kgen.init(keysize, random)
        val secretKey = kgen.generateKey()
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

    companion object {

        val Aes128 = AesUtil(128)
        val Aes192 = AesUtil(192)
        val Aes256 = AesUtil(256)
    }
}