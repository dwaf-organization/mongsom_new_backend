package com.mongsom.dev.dto.admin.user.reqDto;

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
public class MileageChargeReqDto {
    
    @NotNull(message = "사용자 코드는 필수입니다.")
    private Long userCode;
    
    @NotNull(message = "충전 금액은 필수입니다.")
    @Positive(message = "충전 금액은 0보다 커야 합니다.")
    private Integer chargeAmount;
}