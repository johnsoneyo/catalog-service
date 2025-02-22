package de.johnson.catalog_service.repository

import de.johnson.catalog_service.bo.ProductBo
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<ProductBo, Long>