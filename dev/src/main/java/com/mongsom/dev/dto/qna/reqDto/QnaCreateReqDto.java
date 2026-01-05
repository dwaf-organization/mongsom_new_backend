package com.mongsom.dev.dto.qna.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaCreateReqDto {
    
    @NotNull(message = "사용자 코드는 필수입니다.")
    private Long userCode;
    
    private Integer productId; // NULL 허용 (일반 문의)
    
    @Size(max = 255, message = "상품명은 255자 이하여야 합니다.")
    private String productName; // NULL 허용 (일반 문의)
    
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 500, message = "제목은 500자 이하여야 합니다.")
    private String qnaTitle;
    
    @NotBlank(message = "내용은 필수입니다.")
    private String qnaContents;
    
    @Size(max = 100, message = "주문번호는 100자 이하여야 합니다.")
    private String orderId; // NULL 허용
    
    @NotNull(message = "잠금 상태는 필수입니다.")
    @Min(value = 0, message = "잠금 상태는 0 또는 1이어야 합니다.")
    @Max(value = 1, message = "잠금 상태는 0 또는 1이어야 합니다.")
    private Integer lockStatus; // 0=공개, 1=비밀글
}