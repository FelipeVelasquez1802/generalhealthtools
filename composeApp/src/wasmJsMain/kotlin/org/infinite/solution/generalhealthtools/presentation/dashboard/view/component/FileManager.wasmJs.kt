package org.infinite.solution.generalhealthtools.presentation.dashboard.view.component

import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal actual class FileManager actual constructor() {
    actual suspend fun selectFile(): FileHandle? {
        return suspendCoroutine { continuation ->
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"

            input.onchange = { event ->
                val file = input.files?.item(0)
                if (file != null) {
                    val reader = FileReader()
                    reader.onload = {
                        val arrayBuffer = reader.result as ArrayBuffer
                        val bytes = Int8Array(arrayBuffer).toByteArray()
                        continuation.resume(FileHandle(file.name, "", bytes))
                    }
                    reader.readAsArrayBuffer(file)
                }
            }
            input.click()
        }
    }

    actual suspend fun downloadFile(
        bytes: ByteArray,
        fileName: String,
        newFileExtension: String,
    ) {
        val uint8Array = Int8Array(bytes.size)
        bytes.forEachIndexed { index, byte ->
            uint8Array[index] = byte
        }
        val blob = Blob(
            arrayOf(uint8Array).toJsArray(),
            BlobPropertyBag("application/octet-stream")
        )
        val url = URL.createObjectURL(blob)

        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        val fileWithoutExtension = fileName.substringBeforeLast(".")
        anchor.download = "${fileWithoutExtension}.$newFileExtension"
        anchor.style.display = "none"

        document.body?.appendChild(anchor)
        anchor.click()

        document.body?.removeChild(anchor)
        URL.revokeObjectURL(url)
    }
}

private fun Int8Array.toByteArray(): ByteArray {
    return ByteArray(this.length) { this[it] }
}

private fun Array<Int8Array>.toJsArray(): JsArray<JsAny?> {
    val result = JsArray<JsAny?>()
    forEachIndexed { index, item -> result[index] = item }
    return result
}