package de.johnson.catalog_service.config

import de.johnson.catalog_service.bo.ProductBo
import de.johnson.catalog_service.data.DataAggregator
import de.johnson.catalog_service.data.DataType
import de.johnson.catalog_service.data.ProductData
import de.johnson.catalog_service.service.ProductService
import de.johnson.catalog_service.service.transformer.Transformers
import de.johnson.catalog_service.service.writer.FormatWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.HeaderEnricherSpec
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlowDefinition
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.handler.LoggingHandler
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessageHeaders
import java.util.concurrent.Executors

@MessagingGateway
interface CatalogProductDataGateway {
    @Gateway(requestChannel = "data.input")
    fun process(aggregator: DataAggregator?)
}

@EnableIntegration
@Configuration
class CatalogProductDataIntegrationConfig {

    @Bean
    fun data(): IntegrationFlow {
        return IntegrationFlow { f: IntegrationFlowDefinition<*> ->
            f // enrich header with output format required when writing files
                .enrichHeaders { h: HeaderEnricherSpec ->
                    h.headerExpression(
                        "formats",
                        "payload.productData.outputFormats"
                    )
                }
                /**
                 * send message to [channel for transformoing][CatalogProductDataIntegrationConfig.entityTransformer]
                 */
                .channel("entityTransformer.input")
        }
    }

    @Bean
    fun entityTransformer(classToEntityTransformer: Map<Class<out ProductData>, Transformers>): IntegrationFlow {
        return IntegrationFlow { f: IntegrationFlowDefinition<*> ->
            f.transform { message: DataAggregator ->
                val transformers = classToEntityTransformer[message.productData::class.java]
                transformers!!.transform(message)
            } // pushes transformed product data list to persistence service channel
                .channel("persistence.input")
        }
    }

    @Bean
    fun persistence(productService: ProductService?): IntegrationFlow {
        return IntegrationFlow { f: IntegrationFlowDefinition<*> ->
            f
                /**
                 * Save [objects][ProductBo]
                 */
                .handle(productService!!)
                .enrichHeaders { h: HeaderEnricherSpec ->
                    h.headerExpression(
                        "savedData",
                        "payload"
                    )
                }
                .log(LoggingHandler.Level.DEBUG)
                .handle { _: Any?, h: MessageHeaders -> h["formats"] } // splits formats and pushes persisted entities to executor channel for async write
                .split()
                .channel(MessageChannels.executor(Executors.newFixedThreadPool(2)))
                .channel("fileSystemStorage.input")
        }
    }


    /**
     * Final integration flow that writes to the file system concurrently
     *
     * @param dataTypeToFormatWriter
     * @return
     */
    @Bean
    fun fileSystemStorage(dataTypeToFormatWriter: Map<DataType, FormatWriter>): IntegrationFlow {
        return IntegrationFlow { f: IntegrationFlowDefinition<*> ->
            f.handle(writeToFileStorage(dataTypeToFormatWriter))
        }
    }

    /**
     * This method writes to file storage by fetching formater writer key in strategu
     *
     * @param dataTypeToFormatWriter
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    private fun writeToFileStorage(dataTypeToFormatWriter: Map<DataType, FormatWriter>): MessageHandler {
        return MessageHandler { message: Message<*> ->
            dataTypeToFormatWriter[message.payload as DataType]?.write(message.headers["savedData"] as List<ProductBo>)
        }
    }
}