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
    
    private Integer option1; // 첫 번째 옵션 (NULL 허용)
    
    private Integer option2; // 두 번째 옵션 (NULL 허용)
    
    @NotNull(message = "수량은 필수입니다.")
    @Positive(message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;
    
    // 옵션 존재 여부 체크
    public boolean hasOption1() {
        return option1 != null;
    }
    
    public boolean hasOption2() {
        return option2 != null;
    }
    
    public boolean hasAnyOptions() {
        return hasOption1() || hasOption2();
    }
}