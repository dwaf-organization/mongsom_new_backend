package com.mongsom.dev.dto.change.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeCreateReqDto {
    
    @NotNull(message = "주문 상세 ID는 필수입니다.")
    private Integer orderDetailId;
    
    @NotNull(message = "사용자 코드는 필수입니다.")
    private Long userCode;
    
    @NotBlank(message = "교환/반품 유형은 필수입니다.")
    @Pattern(regexp = "^(교환|반품)$", message = "교환/반품 유형은 '교환' 또는 '반품'만 가능합니다.")
    private String changeType; // 교환, 반품
    
    @NotBlank(message = "교환/반품 사유는 필수입니다.")
    @Size(max = 500, message = "교환/반품 사유는 500자를 초과할 수 없습니다.")
    private String reason;
    
    @Size(max = 50, message = "은행명은 50자를 초과할 수 없습니다.")
    private String refundBank; // 반품시 은행명 (선택)
    
    @Size(max = 100, message = "계좌번호는 100자를 초과할 수 없습니다.")
    private String refundAccount; // 반품시 계좌번호 (선택)
}