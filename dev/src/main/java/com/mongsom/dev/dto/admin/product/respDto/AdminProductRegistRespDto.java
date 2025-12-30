package com.mongsom.dev.dto.admin.product.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductRegistRespDto {
    
    private Integer productId;
    private String message;
    
    // 성공 응답 생성
    public static AdminProductRegistRespDto success(Integer productId) {
        return AdminProductRegistRespDto.builder()
                .productId(productId)
                .message("상품이 성공적으로 등록되었습니다.")
                .build();
    }
    
    // 실패 응답 생성
    public static AdminProductRegistRespDto failure(String message) {
        return AdminProductRegistRespDto.builder()
                .productId(null)
                .message(message)
                .build();
    }
}