package zyxel.com.multyproneo.tool

import android.util.Base64
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by LouisTien on 2019/6/18.
 */
object CryptTool
{
    val IvAESDefault = "1234567890abcdef"
    val KeyAESDefault = "zyxeloneconncet_zyxeloneconnect_"
    var IvAES = IvAESDefault
    var KeyAES = KeyAESDefault

    fun EncryptAES(iv: ByteArray, key: ByteArray, text: ByteArray): String?
    {
        val encryptedData: String
        try
        {
            val mAlgorithmParameterSpec = IvParameterSpec(iv) as AlgorithmParameterSpec
            val mSecretKeySpec = SecretKeySpec(key, "AES")
            val mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            mCipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec, mAlgorithmParameterSpec)
            val encryptTextByte = mCipher.doFinal(text)
            encryptedData = Base64.encodeToString(encryptTextByte, Base64.DEFAULT)
        }
        catch(ex: Exception)
        {
            return null
        }
        return encryptedData
    }

    fun DecryptAES(iv: ByteArray, key: ByteArray, text: ByteArray): String?
    {
        val decryptedData: String
        try
        {
            val mAlgorithmParameterSpec = IvParameterSpec(iv) as AlgorithmParameterSpec
            val mSecretKeySpec = SecretKeySpec(key, "AES")
            val mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mAlgorithmParameterSpec)
            val decryptionTextByte = mCipher.doFinal(text)
            decryptedData = String(decryptionTextByte, charset("UTF-8"))
        }
        catch(ex: Exception)
        {
            return null
        }
        return decryptedData
    }

    fun getRandomString(length: Int): String
    {
        val str = "abcdefghigklmnopkrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        val sf = StringBuffer()
        for(i in 0 until length)
        {
            val number = random.nextInt(62)//0~61
            sf.append(str[number])
        }
        return sf.toString()
    }
}