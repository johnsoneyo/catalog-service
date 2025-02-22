package de.johnson.catalog_service.data

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

class DataAggregator(val productData: ProductData, val file: ByteArray)

enum class DataType {
    JSON, CSV, XML
}

class CSVProductData(outputFormats: ArrayList<DataType>) : ProductData(outputFormats)
class JSONProductData(outputFormats: ArrayList<DataType>) : ProductData(outputFormats)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "dataType")
@JsonSubTypes(
    JsonSubTypes.Type(value = CSVProductData::class, name = "CSV"),
    JsonSubTypes.Type(value = JSONProductData::class, name = "JSON")
)
abstract class ProductData(val outputFormats: ArrayList<DataType>)