package org.bodsrisk.utils

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.slink.number.formatGrouped
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.apache.commons.csv.QuoteMode
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader

private val log = LoggerFactory.getLogger("org.bodsrisk.utils.csv")

val defaultCsvFormat = CSVFormat.RFC4180.builder()
    .setHeader()
    .setSkipHeaderRecord(true)
    .setIgnoreSurroundingSpaces(true)
    .build()

fun csvPrintFormat(vararg headers: String) = CSVFormat.DEFAULT
    .builder()
    .setHeader(*headers)
    .setQuoteMode(QuoteMode.ALL)
    .setRecordSeparator("\n")
    .build()

val jacksonCsvMapper = CsvMapper().apply {
    registerKotlinModule()
    registerModule(JavaTimeModule())
}

inline fun <reified T> File.readCsv(): List<T> {
    val records = mutableListOf<T>()
    FileReader(this).use { reader ->
        jacksonCsvMapper
            .readerFor(T::class.java)
            .with(CsvSchema.emptySchema().withHeader())
            .readValues<T>(reader)
            .forEach { record ->
                records.add(record)
            }
    }
    return records
}

fun File.forEachCsvRecord(format: CSVFormat = defaultCsvFormat, block: (CSVRecord) -> Unit) {
    var count = 0
    CSVParser(FileReader(this), format).forEach { csvRecord ->
        block(csvRecord)
        count++
        if (count % 100_000 == 0) {
            log.info("Processed ${count.formatGrouped()} CSV records from $this")
        }
    }
    log.info("Processed a total of ${count.formatGrouped()} CSV records from $this")
}