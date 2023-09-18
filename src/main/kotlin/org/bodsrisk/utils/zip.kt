package org.bodsrisk.utils

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

private val log = LoggerFactory.getLogger("org.bodsrisk.utils.zip")

fun File.unTarGz(output: File) {
    inputStream().use { inputStream ->
        inputStream.unTarGz(output)
    }
}

fun InputStream.unTarGz(output: File) {
    val bi = BufferedInputStream(this)
    val gzi = GzipCompressorInputStream(bi)
    TarArchiveInputStream(gzi).use { archive ->
        archive.nextEntry
        output.outputStream().use {
            archive.copyTo(it)
        }
    }
}

fun File.unzip(destDir: File, charset: Charset = StandardCharsets.UTF_8) {
    this.inputStream()
        .use { it.unzip(destDir, charset) }
}

fun InputStream.unzip(destDir: File, charset: Charset = StandardCharsets.UTF_8) {
    val zis = ZipInputStream(this, charset)
    var zipEntry = zis.nextEntry
    while (zipEntry != null) {
        val newFile = File(destDir, zipEntry.name)

        log.info("Unzipping entry ${zipEntry.name} to ${newFile.absolutePath}")
        if (zipEntry.isDirectory) {
            if (!newFile.isDirectory && !newFile.mkdirs()) {
                throw IllegalStateException("Could not create directory $newFile")
            }
        } else {
            val parent = newFile.parentFile
            if (!parent.isDirectory && !parent.mkdirs()) {
                throw IllegalStateException("Could not create directory $parent")
            }
            newFile.outputStream().use { fos ->
                IOUtils.copy(zis, fos)
            }
        }
        zipEntry = zis.nextEntry
    }
}

fun File.gunzip(output: File) {
    FileInputStream(this).use { inputStream ->
        inputStream.gunzip(output)
    }
}

fun InputStream.gunzip(output: File) {
    GZIPInputStream(this).use { gzip ->
        val buffer = ByteArray(128 * 1024)
        FileOutputStream(output).use { out ->
            var len: Int
            while (gzip.read(buffer).also { len = it } > 0) {
                out.write(buffer, 0, len)
            }
        }
    }
}
