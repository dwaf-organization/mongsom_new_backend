package com.mongsom.dev.dto.review.respDto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewListRespDto {
    
    private List<AdminReviewItemDto> reviews;
    private PaginationDto pagination;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminReviewItemDto {
        private Integer reviewId;          // 리뷰 ID
        private Long userCode;             // 사용자 코드
        private String userName;           // 사용자명 (조인해서 가져오기)
        private Integer orderDetailId;     // 주문 상세 ID
        private Integer productId;         // 상품 ID
        private String productName;        // 상품명
        private Integer orderId;           // 주문 ID
        private String orderNum;           // 주문 번호

        private Integer option1;           // 첫 번째 옵션 ID
        private Integer option2;           // 두 번째 옵션 ID
        private String option1Name;       // 첫 번째 옵션 이름
        private String option2Name;       // 두 번째 옵션 이름
        private String optionSummary;     // 옵션 요약 (예: "500ml, 블랙")
        
        private Integer reviewRating;      // 리뷰 평점
        private String reviewContent;      // 리뷰 내용 (요약)
        private List<String> reviewImgUrls; // 리뷰 이미지들
        private Integer adminHidden;       // 숨김 상태 (0=정상, 1=숨김)
        // 관리자 답변
        private String adminAnswer;        // 관리자 답변
        private String adminAnswerAt;      // 관리자 답변일자 (YYYY-MM-DD)
        
        private String hiddenStatus;       // 숨김 상태 텍스트
        private LocalDateTime createdAt;   // 리뷰 작성일
        private LocalDateTime updatedAt;   // 수정일
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationDto {
        private Integer currentPage;
        private Integer totalPage;
        private Integer size;
        private Long totalElements;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }
}