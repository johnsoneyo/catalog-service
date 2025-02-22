package de.johnson.catalog_service.controller

import de.johnson.catalog_service.config.CatalogProductDataGateway
import de.johnson.catalog_service.data.DataAggregator
import de.johnson.catalog_service.data.DataType
import de.johnson.catalog_service.data.ProductData
import de.johnson.catalog_service.exception.DataBeanValidationException
import de.johnson.catalog_service.exception.DataTransformerException
import de.johnson.catalog_service.exception.Error
import de.johnson.catalog_service.utils.CATALOG_PRODUCTS
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException


@RequestMapping(value = [CATALOG_PRODUCTS])
@RestController
class CatalogProductDataController(val gateway: CatalogProductDataGateway) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Throws(IOException::class)
    fun process(@RequestPart productData: ProductData, @RequestPart("file") file: MultipartFile) =
        gateway.process(DataAggregator(productData, file.bytes))

    /**
     * File formats to be exported are actual implementation, and it is best
     * that it is being read from an implementation class that defines it
     * @see DataType
     *
     * @return list of export formats supported by the system
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/file-export-types")
    fun exportTypes(): List<DataType> = DataType.entries
}


@ControllerAdvice
class ResponseAdvice {
    @ExceptionHandler(DataTransformerException::class)
    fun dataTransformerException(dataTransformerException: DataTransformerException): ResponseEntity<Error> =
        ResponseEntity<Error>(dataTransformerException.error, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(DataBeanValidationException::class)
    fun dataValidationException(dataTransformerException: DataBeanValidationException): ResponseEntity<DataBeanValidationException.ValidationError> =
        ResponseEntity<DataBeanValidationException.ValidationError>(
            dataTransformerException.error,
            HttpStatus.BAD_REQUEST
        )
}