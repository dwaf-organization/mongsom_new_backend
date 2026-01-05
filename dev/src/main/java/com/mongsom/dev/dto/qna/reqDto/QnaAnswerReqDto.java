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
public class QnaAnswerReqDto {
    
    @NotNull(message = "QNA 코드는 필수입니다.")
    private Integer qnaCode;
    
    @NotBlank(message = "답변 내용은 필수입니다.")
    private String answerContents;
}