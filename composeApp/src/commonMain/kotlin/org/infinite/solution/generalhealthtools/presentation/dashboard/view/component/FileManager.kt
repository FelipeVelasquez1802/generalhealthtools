package org.infinite.solution.generalhealthtools.presentation.dashboard.view.component

internal expect class FileManager private constructor() {
    suspend fun generatePublicKey(): FileManager
    suspend fun selectFile(): FileHandle?
    suspend fun encryptFile(fileBytes: ByteArray): EncryptedData
    suspend fun saveEncryptedFile(encryptedData: EncryptedData, fileName: String): Boolean
    suspend fun downloadFile(
        bytes: ByteArray,
        fileName: String,
        newFileExtension: String,
    )

    companion object{
        fun getInstance(publicKeyString: String): FileManager
    }
}

data class FileHandle(
    val name: String,
    val path: String,
    val bytes: ByteArray,
)

data class EncryptedData(
    val encryptedContent: ByteArray,
    val encryptedAesKey: ByteArray,
    val iv: ByteArray,
)

data class KeyData(
    val encryptedAesKey: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as KeyData

        if (!encryptedAesKey.contentEquals(other.encryptedAesKey)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = encryptedAesKey.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}
