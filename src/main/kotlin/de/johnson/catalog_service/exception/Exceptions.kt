package de.johnson.catalog_service.exception

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

enum class Type {
    VALIDATION, ERROR
}

class ErrorDto @JsonCreator constructor(
    @param:JsonProperty("type") val type: Type,
    @JsonProperty("errors") vararg val errors: Array<String>
)

@JvmRecord
data class Error @JsonCreator constructor(@param:JsonProperty("error") val error: ErrorDto)

class DataBeanValidationException
/**
 * This constructor is used to manage bean validation exceptions
 * @param message
 * @param error
 */(message: String, val error: ValidationError) : RuntimeException(message) {

    data class ValidationError @JsonCreator constructor(
        @param:JsonProperty("type") val type: Type, @param:JsonProperty(
            "errors"
        ) val errors: Map<String, Set<String>>
    )
}


class DataTransformerException : java.lang.RuntimeException {
    val error: Error

    /**
     * This constructor is used to manage application exceptions
     * @param message
     * @param error
     */
    constructor(message: String, error: Error) : super(message) {
        this.error = error
    }

    constructor(message: String, error: Error, cause: Throwable) : super(message, cause) {
        this.error = error
    }
}
