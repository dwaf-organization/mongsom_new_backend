package com.mongsom.dev.dto.qna.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductQnaListRespDto {
    
    private List<QnaDetailDto> qnaList;
    private PaginationInfo pagination;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QnaDetailDto {
        // QNA 테이블의 모든 컬럼
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
        private Boolean isLocked;       // 비밀글 여부
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Integer currentPage;
        private Integer pageSize;
        private Integer totalPages;
        private Long totalElements;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }
}