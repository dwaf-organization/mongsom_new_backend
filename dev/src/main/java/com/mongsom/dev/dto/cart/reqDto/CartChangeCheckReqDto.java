package com.mongsom.dev.dto.cart.reqDto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartChangeCheckReqDto {
    
    @NotNull(message = "장바구니 ID는 필수입니다.")
    private Integer cartId;
}