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
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

internal actual class FileManager actual constructor() {

    private lateinit var publicKey: PublicKey
    private var publicKeyByteArray: ByteArray = ByteArray(0)

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
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val aesKey = keyGen.generateKey()
        val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }

        return EncryptedData(
            cipherAes = cipherAES(fileBytes, aesKey, iv),
            cipherRsa = cipherRSA(aesKey),
            iv = iv,
        )
    }

    private fun cipherAES(
        fileBytes: ByteArray,
        encryptedAesKey: SecretKey,
        iv: ByteArray
    ): ByteArray {
        val aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        aesCipher.init(Cipher.ENCRYPT_MODE, encryptedAesKey, IvParameterSpec(iv))
        return aesCipher.doFinal(fileBytes)
    }

    private fun cipherRSA(
        fileBytes: SecretKey,
    ): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(fileBytes.encoded)
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

                outputFile.writeBytes(encryptedData.cipherAes)

                val keyFile = File(saveDialog.directory, "${fileName}.key")
                keyFile.writeBytes(encryptedData.cipherRsa)
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
            val pemContent = String(publicKeyByteArray)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\n", "")
                .trim()

            val keyBytes = Base64.getDecoder().decode(pemContent)
            val keySpec = X509EncodedKeySpec(keyBytes)
            publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec)
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
        actual fun getInstance(publicKeyByteArray: ByteArray): FileManager {
            return FileManager().apply {
                this.publicKeyByteArray = publicKeyByteArray
            }
        }
    }


}