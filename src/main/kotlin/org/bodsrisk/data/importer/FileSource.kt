package org.bodsrisk.data.importer

import io.slink.files.TempDir
import io.slink.files.withTempDir
import io.slink.http.get
import io.slink.http.newHttpClient
import io.slink.http.writeTo
import org.bodsrisk.utils.gunzip
import org.bodsrisk.utils.unTarGz
import org.bodsrisk.utils.unzip
import org.slf4j.LoggerFactory
import java.io.File

sealed class FileSource {

    private var unpack: Unpack? = null

    protected abstract fun fetch(tempDir: TempDir): File

    fun unpack(unpack: Unpack): FileSource {
        this.unpack = unpack
        return this
    }

    fun forEachFile(block: (File) -> Unit) {
        withTempDir(File("temp")) { tempDir ->
            loadFiles(tempDir).forEach { file ->
                block(file)
            }
        }
    }

    fun loadFiles(tempDir: TempDir): List<File> {
        log.info("Processing file source $this")
        val sourceFile = fetch(tempDir)
        val files = mutableListOf<File>()

        when (unpack) {
            Unpack.TAR_GZ -> {
                val unpackedFile = tempDir.newFile()
                log.info("Un-tar-gz $sourceFile to $unpackedFile")
                sourceFile.unTarGz(unpackedFile)
                files.add(unpackedFile)
            }

            Unpack.GZIP -> {
                val unpackedFile = tempDir.newFile()
                log.info("Gunzip $sourceFile to $unpackedFile")
                sourceFile.gunzip(unpackedFile)
                files.add(unpackedFile)
            }

            Unpack.ZIP -> {
                val unzipDir = tempDir.newFile()
                log.info("Un-zipping $sourceFile to $unzipDir")
                sourceFile.unzip(unzipDir)
                files.addAll(unzipDir.listFiles())
            }

            null -> {
                files.add(sourceFile)
            }
        }

        return files
    }

    class Remote(val url: String) : FileSource() {
        override fun fetch(tempDir: TempDir): File {
            val file = tempDir.newFile()
            newHttpClient().get(url).writeTo(file)
            return file
        }

        override fun toString(): String {
            return "Remote(url='$url')"
        }
    }

    class Local(val path: String) : FileSource() {
        override fun fetch(tempDir: TempDir): File {
            return File(path)
        }

        override fun toString(): String {
            return "Local(path='$path')"
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FileSource::class.java)

    }
}
