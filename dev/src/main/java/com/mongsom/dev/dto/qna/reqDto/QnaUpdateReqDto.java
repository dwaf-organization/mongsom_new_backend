package com.mongsom.dev.dto.qna.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaUpdateReqDto {
    
    @NotNull(message = "QNA 코드는 필수입니다.")
    private Integer qnaCode;
    
    @NotNull(message = "사용자 코드는 필수입니다.")
    private Long userCode;
    
    @NotBlank(message = "내용은 필수입니다.")
    private String qnaContents;
}