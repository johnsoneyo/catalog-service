package de.johnson.catalog_service.config

import de.johnson.catalog_service.data.DataType
import de.johnson.catalog_service.data.ProductData
import de.johnson.catalog_service.service.transformer.Transformers
import de.johnson.catalog_service.service.writer.FormatWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import kotlin.collections.HashMap


@Configuration
class BeanStrategyRegistryConfig() {

    @Bean
    fun classToEntityTransformer(transformers: Array<Transformers>): Map<Class<out ProductData>, Transformers> {
        val entityTransformer: MutableMap<Class<out ProductData>, Transformers> =
            HashMap()
        for (transformer in transformers) {
            entityTransformer[transformer.transformerDataClass()] = transformer
        }
        return entityTransformer
    }

    @Bean
    fun formatterStrategy(formatters: Array<FormatWriter>): Map<DataType, FormatWriter> {

        val dataTypeToFormater: MutableMap<DataType, FormatWriter> = EnumMap(DataType::class.java)
        for (formatter in formatters) {
            dataTypeToFormater[formatter.format()] = formatter
        }
        return dataTypeToFormater
    }
}