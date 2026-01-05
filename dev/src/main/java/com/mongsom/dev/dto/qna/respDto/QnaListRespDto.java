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
public class QnaListRespDto {
    
    private List<QnaItemDto> qnaList;
    private PaginationInfo pagination;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QnaItemDto {
        private Integer qnaCode;        // QNA 코드
        private Long userCode;          // 사용자 코드 (추가됨)
        private Integer productId;      // 상품 ID (product_code)
        private String productName;     // 상품명
        private String qnaTitle;        // QNA 제목
        private String qnaContents;     // QNA 내용
        private String qnaWriter;       // 작성자
        private String answerStatus;    // "미답변" or "답변완료"
        private String answerContents;
        private Integer lockStatus;     // 0=공개글, 1=비밀글 (수정됨)
        private LocalDateTime createdDate; // 생성일
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Integer currentPage;    // 현재 페이지 (0부터 시작)
        private Integer pageSize;       // 페이지 크기
        private Integer totalPages;     // 전체 페이지 수
        private Long totalElements;     // 전체 요소 수
        private Boolean hasNext;        // 다음 페이지 존재 여부
        private Boolean hasPrevious;    // 이전 페이지 존재 여부
    }
}