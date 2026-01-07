package com.mongsom.dev.dto.review.reqDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAnswerCreateReqDto {
    
    @NotNull(message = "리뷰 ID는 필수입니다.")
    private Integer reviewId;
    
    @NotBlank(message = "관리자 답변은 필수입니다.")
    private String adminAnswer;
}