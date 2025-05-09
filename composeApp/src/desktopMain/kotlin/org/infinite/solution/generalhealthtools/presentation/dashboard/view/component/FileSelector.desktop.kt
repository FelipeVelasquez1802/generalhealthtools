package org.infinite.solution.generalhealthtools.presentation.dashboard.view.component

import androidx.compose.ui.awt.ComposeWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.io.File
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal actual class FileManager actual constructor() {

    private lateinit var keyPair: KeyPair
    private val ivSize = 16
    private val keySize = 256

    init {
        initializeRSAKeys()
    }

    actual suspend fun selectFile(): FileHandle? = withContext(Dispatchers.Main) {
        return@withContext try {
            val fileDialog = FileDialog(ComposeWindow()).apply {
                mode = FileDialog.LOAD
                isVisible = true
            }
            if (fileDialog.file != null) {
                val selectedFile = File(fileDialog.directory, fileDialog.file)
                FileHandle(
                    name = selectedFile.name,
                    path = selectedFile.absolutePath,
                    bytes = selectedFile.readBytes(),
                )
            } else {
                null
            }
        } catch (exception: Exception) {
            null
        }
    }


    actual suspend fun encryptFile(fileBytes: ByteArray): EncryptedData {
        val aesKey = ByteArray(keySize / 8)
        SecureRandom().nextBytes(aesKey)

        val iv = ByteArray(ivSize)
        SecureRandom().nextBytes(iv)

        return EncryptedData(
            encryptedContent = cipherAES(fileBytes, aesKey, iv),
            encryptedAesKey = cipherRSA(aesKey, keyPair.public.encoded),
            iv = iv,
        )
    }

    private fun cipherRSA(
        fileBytes: ByteArray,
        publicKey: ByteArray,
    ): ByteArray {
        val keySpec = X509EncodedKeySpec(publicKey)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKeyGenerated = keyFactory.generatePublic(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKeyGenerated)
        return cipher.doFinal(fileBytes)
    }

    private fun cipherAES(
        fileBytes: ByteArray,
        encryptedAesKey: ByteArray,
        iv: ByteArray
    ): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(encryptedAesKey, "AES")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        return cipher.doFinal(fileBytes)
    }

    actual suspend fun saveEncryptedFile(
        encryptedData: EncryptedData,
        fileName: String
    ): Boolean = withContext(Dispatchers.Main) {
        try {
            val saveDialog = FileDialog(ComposeWindow()).apply {
                mode = FileDialog.SAVE
                file = "${fileName}.enc"
                isVisible = true
            }
            if (saveDialog.file != null) {
                val outputFile = File(saveDialog.directory, saveDialog.file)

                outputFile.writeBytes(encryptedData.encryptedContent)

                val keyFile = File(saveDialog.directory, "${fileName}.key")
                val keyData = KeyData(
                    encryptedAesKey = encryptedData.encryptedAesKey,
                    iv = encryptedData.iv,
                )
                keyFile.writeBytes(serializeKeyData(keyData))
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    actual suspend fun downloadFile(
        bytes: ByteArray,
        fileName: String,
        newFileExtension: String
    ) {

    }

    private fun initializeRSAKeys() {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        keyPair = keyPairGenerator.generateKeyPair()
    }

    private fun serializeKeyData(keyData: KeyData): ByteArray {
        return buildList {
            addAll(keyData.encryptedAesKey.toList())
            add(0xFF.toByte())
            addAll(keyData.iv.toList())
        }.toByteArray()
    }

}