package com.mongsom.dev.dto.cart.reqDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartAddReqDto {
    
    @NotNull(message = "사용자 코드는 필수입니다.")
    private Long userCode;
    
    @NotNull(message = "상품 ID는 필수입니다.")
    private Integer productId;
    
    private Integer combinationId; // 옵션 조합 ID (NULL 허용)
    
    @NotNull(message = "수량은 필수입니다.")
    @Positive(message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;
}