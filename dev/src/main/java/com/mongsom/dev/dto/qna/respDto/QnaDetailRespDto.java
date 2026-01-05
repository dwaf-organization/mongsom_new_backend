package com.mongsom.dev.dto.qna.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaDetailRespDto {
    
    private Integer qnaCode;        // QNA 코드
    private Long userCode;          // 사용자 코드
    private Integer productCode;    // 상품 코드
    private String productName;     // 상품명
    private String qnaTitle;        // QNA 제목
    private String qnaWriter;       // 작성자
    private String qnaContents;     // QNA 내용
    private String answerContents;  // 답변 내용
    private String answerAt;        // 답변 작성일
    private String orderId;         // 주문번호
    private Integer lockStatus;     // 잠금 상태 (0=공개, 1=비밀글)
    private LocalDateTime createdAt; // 생성일
    private LocalDateTime updatedAt; // 수정일
    
    // 추가 상태 정보
    private String answerStatus;    // "미답변" or "답변완료"
    private Boolean isLocked;       // 비밀글 여부 (편의용)
}