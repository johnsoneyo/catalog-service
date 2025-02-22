package de.johnson.catalog_service.bo

import de.johnson.catalog_service.service.transformer.validation.DPBValidationGroup
import de.johnson.catalog_service.service.transformer.validation.DQRValidationGroup
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull


@Table(name = "product")
@Entity
data class ProductBo(
    @field:Column(name = "product_type") var productType: @NotEmpty(
        groups = [DPBValidationGroup::class, DQRValidationGroup::class]
    ) String?,
    @field:Column var name: @NotEmpty(groups = [DPBValidationGroup::class, DQRValidationGroup::class]) String?,
    @field:Column(
        name = "short_description"
    ) var shortDescription: @NotEmpty(groups = [DQRValidationGroup::class]) String?,
    @field:Column(
        name = "long_description"
    ) var longDescription: @NotEmpty(groups = [DQRValidationGroup::class]) String?,
    @field:Column(
        name = "class_code"
    ) var classCode: @NotEmpty(groups = [DPBValidationGroup::class, DQRValidationGroup::class]) String?,
    @field:Column(
        name = "supplier_id"
    ) var supplierId: @NotEmpty(groups = [DQRValidationGroup::class]) String?,
    @field:Column(
        name = "customs_number"
    ) var customsNumber: @NotNull(groups = [DPBValidationGroup::class, DQRValidationGroup::class]) @Min(
        value = 1,
        groups = [DPBValidationGroup::class, DQRValidationGroup::class]
    ) Long?
) {
    @Id
    @GeneratedValue
    var id: Long? = null
}