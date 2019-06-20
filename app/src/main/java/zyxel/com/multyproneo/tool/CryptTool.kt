package zyxel.com.multyproneo.tool

import android.util.Base64
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by LouisTien on 2019/6/18.
 */
class CryptTool
{
    companion object
    {
        val IvAES = "1234567890abcdef"
        val KeyAES = "zyxeloneconncet_zyxeloneconnect_"

        fun EncryptAES(iv: ByteArray, key: ByteArray, text: ByteArray): String?
        {
            var encryptedData: String
            try
            {
                var mAlgorithmParameterSpec = IvParameterSpec(iv) as AlgorithmParameterSpec
                var mSecretKeySpec = SecretKeySpec(key, "AES")
                var mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                mCipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec, mAlgorithmParameterSpec)
                var encryptTextByte = mCipher.doFinal(text)
                encryptedData = Base64.encodeToString(encryptTextByte, Base64.DEFAULT)
            }
            catch(ex: Exception)
            {
                return null;
            }
            return encryptedData
        }

        fun DecryptAES(iv: ByteArray, key: ByteArray, text: ByteArray): String?
        {
            var decryptedData: String
            try
            {
                var mAlgorithmParameterSpec = IvParameterSpec(iv) as AlgorithmParameterSpec
                var mSecretKeySpec = SecretKeySpec(key, "AES")
                var mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mAlgorithmParameterSpec)
                var decryptionTextByte = mCipher.doFinal(text)
                decryptedData = String(decryptionTextByte, charset("UTF-8"))
            }
            catch(ex: Exception)
            {
                return null
            }
            return decryptedData
        }
    }
}