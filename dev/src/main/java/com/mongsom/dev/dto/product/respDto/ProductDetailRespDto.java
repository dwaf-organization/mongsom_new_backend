package com.mongsom.dev.dto.product.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailRespDto {
    
    // === 기본 상품 정보 ===
    private Integer productId;
    private String name;
    private String contents;
    private Integer premium; // 0=일반, 1=프리미엄
    private Integer basePrice;
    private Integer salesMargin;
    private Integer discountPer;
    private Integer discountPrice;
    private Integer deliveryPrice;
    private Integer stockStatus; // 0=품절, 1=주문가능, 2=부분주문가능
    private Integer isAvailable; // 0=판매중단, 1=판매중
    private Integer deleteStatus; // 0=정상, 1=삭제
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // === 연관 정보 ===
    private List<ProductImageDto> productImages;
    private List<OptionTypeDto> optionTypes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageDto {
        private Integer productImgId;
        private String productImgUrl;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionTypeDto {
        private Integer optionTypeId;
        private String typeName;
        private Integer isRequired; // 0=선택, 1=필수
        private Integer sortOrder;
        private List<OptionValueDto> optionValues;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionValueDto {
        private Integer optionValueId;
        private String valueName;
        private Integer priceAdjustment;
        private Integer stockStatus; // 0=품절, 1=주문가능
        private Integer sortOrder;
    }
    
    // === 상태 체크 메서드 ===
    public boolean isPremiumProduct() {
        return premium != null && premium == 1;
    }
    
    public boolean hasDiscount() {
        return discountPer != null && discountPer > 0;
    }
    
    public boolean isInStock() {
        return stockStatus != null && stockStatus > 0;
    }
    
    public boolean isAvailableForSale() {
        return isAvailable != null && isAvailable == 1;
    }
    
    public boolean isDeleted() {
        return deleteStatus != null && deleteStatus == 1;
    }
    
    public boolean hasOptions() {
        return optionTypes != null && !optionTypes.isEmpty();
    }
    
    // === 정적 팩토리 메서드 ===
    public static ProductDetailRespDto failure(String message) {
        return ProductDetailRespDto.builder()
                .name(message) // 에러 메시지를 name 필드에 임시 저장
                .build();
    }
}