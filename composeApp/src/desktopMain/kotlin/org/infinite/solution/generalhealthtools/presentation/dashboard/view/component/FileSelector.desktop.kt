package org.infinite.solution.generalhealthtools.presentation.dashboard.view.component

import androidx.compose.ui.awt.ComposeWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.io.File
import java.security.KeyFactory
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal actual class FileManager actual constructor() {

    private lateinit var publicKey: PublicKey
    private var publicKeyString: String = ""
    private val ivSize = 16
    private val keySize = 256
    private val rsaKeySize = 2048

    actual suspend fun generatePublicKey(): FileManager = apply {
        loadRSAPublicKey()
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
        } catch (_: Exception) {
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
            encryptedAesKey = cipherRSA(aesKey),
            iv = iv,
        )
    }

    private fun cipherRSA(
        fileBytes: ByteArray,
    ): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
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

    private fun loadRSAPublicKey() {
        try {
            val publicKeyB64 = publicKeyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\n", "")
                .replace("\r", "")

            val keyBytes = java.util.Base64.getDecoder().decode(publicKeyB64)

            val keySpec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            publicKey = keyFactory.generatePublic(keySpec)
        } catch (e: Exception) {
            println("Error al cargar la clave pública RSA: ${e.message}")
        }
    }


    private fun serializeKeyData(keyData: KeyData): ByteArray {
        return buildList {
            addAll(keyData.encryptedAesKey.toList())
            add(0xFF.toByte())
            addAll(keyData.iv.toList())
        }.toByteArray()
    }

    actual companion object {
        actual fun getInstance(publicKeyString: String): FileManager {
            return FileManager().apply {
                this.publicKeyString = publicKeyString
            }
        }
    }


}