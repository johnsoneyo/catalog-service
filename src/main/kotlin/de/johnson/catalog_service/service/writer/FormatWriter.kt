package de.johnson.catalog_service.service.writer

import de.johnson.catalog_service.bo.ProductBo
import de.johnson.catalog_service.data.DataType
import de.johnson.catalog_service.utils.MAPPER
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

interface FormatWriter {

    /**
     * @param savedProducts
     */
    fun write(savedProducts: List<ProductBo>)

    /**
     * @return format used to register format writer in bean lookup
     */
    fun format(): DataType
}

@Service
class CSVFormatWriter(@Value("\${file.write-path.csv}") val fileWritePath: String) : FormatWriter {
    private val log = LoggerFactory.getLogger(this.javaClass)

    private val CSV_HEADER: String =
        "product_type,name,short_description,long_description,class_code,supplier_id,customs_number"


    override fun write(savedProducts: List<ProductBo>) {
        val csvRows: MutableList<String> = ArrayList()
        csvRows.add(CSV_HEADER)

        try {
            for (p in savedProducts) {
                val cols: MutableList<String> = ArrayList()
                for (entityField in p.javaClass.declaredFields) {
                    entityField.isAccessible = true

                    if (entityField.name == "id") continue
                    val entity = entityField[p]

                    if (entity is String) {
                        if ("," in entity) {
                            // add double quotes to long description text
                            cols.add('"'.plus(entity).plus('"'))
                        } else {
                            cols.add(entity)
                        }
                    } else {
                        cols.add(entity.toString())
                    }
                }
                val csvRow = cols.joinToString(separator = ",")
                csvRows.add(csvRow)
            }

            val csvDocument = java.lang.String.join("\r\n", csvRows)
            val path: Path = Paths.get("$fileWritePath/product.csv")
            val strToBytes = csvDocument.toByteArray()

            Files.write(path, strToBytes)
        } catch (ex: IllegalAccessException) {
            log.error("error occurred writing to file", ex)

        } catch (ex: IOException) {
            log.error("error occurred writing to file", ex)
        }
    }

    override fun format(): DataType = DataType.CSV
}

@Service
class JSONFormatWriter(@Value("\${file.write-path.json}") val fileWritePath: String) : FormatWriter {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun write(savedProducts: List<ProductBo>) {
        try {

            MAPPER.writeValue(File("$fileWritePath/product.json"), savedProducts)
        } catch (ex: Exception) {
            log.error("error occurred writing to file", ex)
        }
    }

    override fun format(): DataType = DataType.JSON
}