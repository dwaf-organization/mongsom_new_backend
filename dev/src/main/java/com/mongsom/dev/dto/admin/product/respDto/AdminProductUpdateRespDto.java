package com.mongsom.dev.dto.admin.product.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductUpdateRespDto {
    
    private Integer productId;
    private String message;
    private UpdateSummary updateSummary;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateSummary {
        private Integer updatedImages;
        private Integer addedImages;
        private Integer deletedImages;
        private Integer updatedOptionTypes;
        private Integer addedOptionTypes;
        private Integer deletedOptionTypes;
        private Integer updatedOptionValues;
        private Integer addedOptionValues;
        private Integer deletedOptionValues;
    }
    
    // 성공 응답 생성
    public static AdminProductUpdateRespDto success(Integer productId, UpdateSummary summary) {
        return AdminProductUpdateRespDto.builder()
                .productId(productId)
                .message("상품이 성공적으로 수정되었습니다.")
                .updateSummary(summary)
                .build();
    }
    
    // 실패 응답 생성
    public static AdminProductUpdateRespDto failure(String message) {
        return AdminProductUpdateRespDto.builder()
                .productId(null)
                .message(message)
                .updateSummary(null)
                .build();
    }
}