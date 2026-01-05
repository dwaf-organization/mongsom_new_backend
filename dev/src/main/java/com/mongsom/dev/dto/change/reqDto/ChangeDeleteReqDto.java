package com.mongsom.dev.dto.change.reqDto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeDeleteReqDto {
    
    @NotNull(message = "주문 상세 ID는 필수입니다.")
    private Integer orderDetailId;
    
    @NotNull(message = "사용자 코드는 필수입니다.")
    private Long userCode;
}