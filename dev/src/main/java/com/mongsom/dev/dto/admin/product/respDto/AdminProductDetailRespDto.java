package com.mongsom.dev.dto.admin.product.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.mongsom.dev.entity.Product;
import com.mongsom.dev.entity.ProductOptionType;
import com.mongsom.dev.entity.ProductOptionValue;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductDetailRespDto {
    
    // 기본 상품 정보
    private Integer productId;
    private String name;
    private String contents;
    private Integer premium;
    private Integer basePrice;
    private Integer salesMargin;
    private Integer discountPer;
    private Integer discountPrice;
    private Integer deliveryPrice;
    private Integer stockStatus;
    private Integer isAvailable;
    private Integer deleteStatus;
    
    // 상품 이미지
    private List<ProductImageDto> productImages;
    
    // 옵션 정보
    private List<OptionTypeDto> optionTypes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageDto {
        private Integer productImgId;
        private String productImgUrl;
        
        public static ProductImageDto from(Integer productImgId, String productImgUrl) {
            return ProductImageDto.builder()
                    .productImgId(productImgId)
                    .productImgUrl(productImgUrl)
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionTypeDto {
        private Integer optionTypeId;
        private String typeName;
        private Integer isRequired;
        private Integer sortOrder;
        private List<OptionValueDto> optionValues;
        
        public static OptionTypeDto from(ProductOptionType optionType) {
            return OptionTypeDto.builder()
                    .optionTypeId(optionType.getOptionTypeId())
                    .typeName(optionType.getTypeName())
                    .isRequired(optionType.getIsRequired())
                    .sortOrder(optionType.getSortOrder())
                    .optionValues(optionType.getOptionValues().stream()
                            .map(OptionValueDto::from)
                            .collect(Collectors.toList()))
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionValueDto {
        private Integer optionValueId;
        private String valueName;
        private Integer priceAdjustment;
        private Integer stockStatus;
        private Integer sortOrder;
        
        public static OptionValueDto from(ProductOptionValue optionValue) {
            return OptionValueDto.builder()
                    .optionValueId(optionValue.getOptionValueId())
                    .valueName(optionValue.getValueName())
                    .priceAdjustment(optionValue.getPriceAdjustment())
                    .stockStatus(optionValue.getStockStatus())
                    .sortOrder(optionValue.getSortOrder())
                    .build();
        }
    }
    
    // 메인 변환 메서드
    public static AdminProductDetailRespDto from(Product product, 
                                                  List<ProductImageDto> productImages,
                                                  List<OptionTypeDto> optionTypes) {
        return AdminProductDetailRespDto.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .contents(product.getContents())
                .premium(product.getPremium())
                .basePrice(product.getBasePrice())
                .salesMargin(product.getSalesMargin())
                .discountPer(product.getDiscountPer())
                .discountPrice(product.getDiscountPrice())
                .deliveryPrice(product.getDeliveryPrice())
                .stockStatus(product.getStockStatus())
                .isAvailable(product.getIsAvailable())
                .deleteStatus(product.getDeleteStatus())
                .productImages(productImages)
                .optionTypes(optionTypes)
                .build();
    }
}