package de.johnson.catalog_service.utils

import com.fasterxml.jackson.databind.ObjectMapper
import de.johnson.catalog_service.bo.ProductBo
import de.johnson.catalog_service.exception.DataBeanValidationException
import de.johnson.catalog_service.exception.Type
import de.johnson.catalog_service.service.transformer.validation.DPBValidationGroup
import de.johnson.catalog_service.service.transformer.validation.DQRValidationGroup
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import org.springframework.util.CollectionUtils
import kotlin.collections.set

const val CATALOG_PRODUCTS = "/catalog-products"
val MAPPER: ObjectMapper = ObjectMapper()

val validator = Validation.buildDefaultValidatorFactory().validator

/**
 * This collection maintains a product type to validation group
 */

val productTypeToValidationGroup: MutableMap<String, Class<*>> = mutableMapOf(
    "DPB" to DPBValidationGroup::class.java, "DQR" to DQRValidationGroup::class.java
)

/**
 * @param products to be validated
 * @throws DataTransformerException is a jsr 308  bean validation fails
 */
fun applyValidationGroupCheck(products: List<ProductBo>) {
    val errors: MutableMap<String, Set<String>> = HashMap()

    for ((index, product) in products.withIndex()) {
        val validationClass = productTypeToValidationGroup[product.productType]

        if (validationClass != null) {
            val violations: Set<ConstraintViolation<ProductBo>> =
                validator.validate(product, validationClass)

            if (!CollectionUtils.isEmpty(violations)) {
                errors["record " + (index + 1)] = violations.map { cv: ConstraintViolation<ProductBo> ->
                    listOf(cv.propertyPath, cv.message)
                        .joinToString(" ")
                }.toSet()

            }
        }
    }

    if (!CollectionUtils.isEmpty(errors)) {

        throw DataBeanValidationException(
            "error occurred at bean validation", DataBeanValidationException.ValidationError(
                Type.VALIDATION,
                errors
            )
        )
    }
}