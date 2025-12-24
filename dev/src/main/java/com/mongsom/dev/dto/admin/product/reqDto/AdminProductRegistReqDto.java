package com.mongsom.dev.dto.admin.product.reqDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductRegistReqDto {
    
    @NotBlank(message = "상품명은 필수입니다.")
    private String name;
    
    private String contents;
    
    @NotNull(message = "프리미엄 여부는 필수입니다.")
    private Integer premium; // 0=일반상품, 1=프리미엄상품
    
    @NotNull(message = "기본 가격은 필수입니다.")
    @Positive(message = "기본 가격은 0보다 커야 합니다.")
    private Integer basePrice;
    
    private Integer salesMargin;
    
    private Integer discountPer;
    
    private Integer discountPrice;
    
    private Integer deliveryPrice;
    
    @NotNull(message = "재고 상태는 필수입니다.")
    private Integer stockStatus; // 0=품절, 1=주문가능, 2=부분주문가능
    
    @NotNull(message = "판매 가능 여부는 필수입니다.")
    private Integer isAvailable; // 0=판매중단, 1=판매중
    
    private List<String> productImgUrls;
    
    @Valid
    private List<OptionTypeDto> optionTypes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionTypeDto {
        
        @NotBlank(message = "옵션 타입명은 필수입니다.")
        private String typeName; // "용량", "색상"
        
        @NotNull(message = "필수 옵션 여부는 필수입니다.")
        private Integer isRequired; // 0=선택, 1=필수
        
        private Integer sortOrder; // 표시 순서
        
        @Valid
        @NotNull(message = "옵션 값 목록은 필수입니다.")
        private List<OptionValueDto> optionValues;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionValueDto {
        
        @NotBlank(message = "옵션 값은 필수입니다.")
        private String valueName; // "350ml", "실버"
        
        private Integer priceAdjustment; // 추가/차감 가격
        
        @NotNull(message = "재고 상태는 필수입니다.")
        private Integer stockStatus; // 0=품절, 1=주문가능
        
        private Integer sortOrder; // 표시 순서
    }
}