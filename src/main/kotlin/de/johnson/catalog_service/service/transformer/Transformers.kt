package de.johnson.catalog_service.service.transformer

import com.fasterxml.jackson.core.type.TypeReference
import de.johnson.catalog_service.bo.ProductBo
import de.johnson.catalog_service.data.CSVProductData
import de.johnson.catalog_service.data.DataAggregator
import de.johnson.catalog_service.data.JSONProductData
import de.johnson.catalog_service.data.ProductData
import de.johnson.catalog_service.exception.DataTransformerException
import de.johnson.catalog_service.exception.ErrorDto
import de.johnson.catalog_service.exception.Type
import de.johnson.catalog_service.utils.MAPPER
import de.johnson.catalog_service.utils.applyValidationGroupCheck
import de.johnson.catalog_service.utils.mapProduct
import io.micrometer.common.util.StringUtils
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets

interface Transformers {

    /**
     * @param dataAggregator combination of catalog product data and output formats to be written to file
     * @return product data entity
     * @throws DataTransformerException when a bean validation is not met
     */
    @Throws(DataTransformerException::class)
    fun transform(dataAggregator: DataAggregator): List<ProductBo>

    /**
     * @return data format class used in registry of entity transformer
     * @see CatalogProductDataIntegrationConfig
     */
    fun transformerDataClass(): Class<out ProductData>
}

@Service
class JSONEntityTransformer : Transformers {

    override fun transform(dataAggregator: DataAggregator): List<ProductBo> {
        try {
            val mappedProducts: List<ProductBo> = MAPPER.readValue(
                dataAggregator.file,
                object : TypeReference<java.util.ArrayList<LinkedHashMap<String, Any>>>() {})
                .map(LinkedHashMap<String, Any>::mapProduct)

            applyValidationGroupCheck(mappedProducts)
            return mappedProducts
        } catch (ex: Exception) {
            throw DataTransformerException(
                "error occurred parsing json",
                de.johnson.catalog_service.exception.Error(ErrorDto(Type.ERROR, arrayOf("could not parse json"))), ex
            )
        }
    }



    override fun transformerDataClass(): Class<out ProductData> = JSONProductData::class.java
}

@Service
class CSVEntityTransformer : Transformers {
    override fun transform(dataAggregator: DataAggregator): List<ProductBo> {

        val csvData = String(dataAggregator.file, StandardCharsets.UTF_8)

        val csvRows =
            csvData.split("\n".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray() // split by ASCII 10 next line

        val products: MutableList<ProductBo> = ArrayList()
        for (i in csvRows.indices) {
            if (i == 0) {
                val header = csvRows[0]
                if (header.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size != 7) {
                    throw DataTransformerException(
                        "error occurred transforming to csv",
                        de.johnson.catalog_service.exception.Error(
                            ErrorDto(
                                Type.VALIDATION,
                                arrayOf("Invalid csv header, csv should contain 7 columns only")
                            )
                        )
                    )
                }

                continue  // skip header
            }
            val csvRow = csvRows[i]
            val cols = csvRow.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                .toTypedArray() // split by comma and double quote

            val productType = cols[0]
            if (StringUtils.isEmpty(productType)) continue

            val customsNumber = cols[6].replace("\\s+".toRegex(), "")
            products.add(
                ProductBo(
                    productType,
                    cols[1],
                    cols[2],
                    cols[3].replace("\"", ""),
                    cols[4],
                    cols[5],
                    if (StringUtils.isNotEmpty(customsNumber)) customsNumber.toLong() else null
                )
            )
        }
        applyValidationGroupCheck(products)
        return products
    }

    override fun transformerDataClass(): Class<out ProductData> = CSVProductData::class.java
}