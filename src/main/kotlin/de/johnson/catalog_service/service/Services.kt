package de.johnson.catalog_service.service

import de.johnson.catalog_service.bo.ProductBo
import de.johnson.catalog_service.repository.ProductRepository
import org.springframework.stereotype.Service


interface ProductService {
    /**
     * @param products
     * @return list of saved products to be written to file
     */
    fun save(products: List<ProductBo>): List<ProductBo>
}

@Service
class DefaultProductService(val productRepository: ProductRepository) : ProductService {

    override fun save(products: List<ProductBo>): List<ProductBo> {
        return productRepository.saveAll(products)
    }
}
