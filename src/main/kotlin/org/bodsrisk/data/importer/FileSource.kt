package org.bodsrisk.data.importer

import io.slink.files.TempDir
import io.slink.http.get
import io.slink.http.newHttpClient
import io.slink.http.writeTo
import org.bodsrisk.utils.gunzip
import org.bodsrisk.utils.unTarGz
import org.bodsrisk.utils.unzip
import org.slf4j.LoggerFactory
import java.io.File

/**
 * A generic source of data for an importer. This class encapsulates both the retrieval of the content
 * for a data source, as well as the unpacking of it, if necessary.
 *
 * The objective is to separate the data loading from the processing, giving the client code the ability
 * to focus on handling the contents of the source (file or files) rather than storage and retrieval semantics.
 */
sealed class FileSource {

    private var unpack: Unpack? = null

    protected abstract fun fetch(tempDir: TempDir): File

    fun unpack(unpack: Unpack): FileSource {
        this.unpack = unpack
        return this
    }

    open fun getFiles(tempDir: TempDir): List<File> {
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

    /**
     * For test purposes only
     */
    class Static(vararg val files: Pair<String, String>) : FileSource() {
        override fun fetch(tempDir: TempDir): File {
            return tempDir.newFile()
        }

        override fun getFiles(tempDir: TempDir): List<File> {
            val allFiles = mutableListOf<File>()
            files.forEach {
                val dir = tempDir.newDirectory()
                val file = File(dir, it.first)
                file.writeText(it.second)
                allFiles.add(file)
            }
            return allFiles
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FileSource::class.java)

    }
}
