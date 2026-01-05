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
public class MyReviewRespDto {
    
    private List<MyReviewItemDto> items;
    private PaginationDto pagination;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyReviewItemDto {
        // 기본 정보
        private Integer orderDetailId;     // 주문 상세 ID
        private Integer productId;         // 상품 ID
        private Integer reviewStatus;      // 리뷰 상태 (0=미작성, 1=작성완료)
        
        // 상품 정보 (가격 제거됨)
        private String productName;        // 상품명
        private List<String> productImgUrls; // 상품 이미지들
        
        // 옵션 정보
        private Integer option1;           // 첫 번째 옵션 ID
        private Integer option2;           // 두 번째 옵션 ID
        private String option1Name;       // 첫 번째 옵션 이름
        private String option2Name;       // 두 번째 옵션 이름
        private List<OptionInfoDto> selectedOptions; // 선택된 옵션들 상세 정보
        
        // 주문 정보
        private Integer quantity;          // 수량
        private String orderNum;           // 주문 번호
        private LocalDateTime orderCreatedAt; // 주문 생성일
        private LocalDateTime paymentAt;   // 결제일시
        private String deliveryStatus;     // 배송 상태
        
        // 리뷰 내용
        private Integer reviewId;          // 리뷰 ID
        private Integer reviewRating;      // 리뷰 평점
        private String reviewContent;      // 리뷰 내용
        private List<String> reviewImgUrls; // 리뷰 이미지들
        private LocalDateTime reviewCreatedAt; // 리뷰 작성일
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionInfoDto {
        private Integer optionValueId;     // 옵션 값 ID
        private String optionTypeName;     // 옵션 타입명 (예: "용량", "색상")
        private String optionValueName;    // 옵션 값명 (예: "500ml", "블랙")
        private Integer priceAdjustment;   // 가격 조정값
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationDto {
        private Integer currentPage;       // 현재 페이지 (0부터 시작)
        private Integer totalPage;         // 전체 페이지 수
        private Integer size;              // 페이지 크기
        private Long totalElements;        // 전체 요소 수
        private Boolean hasNext;           // 다음 페이지 존재 여부
        private Boolean hasPrevious;       // 이전 페이지 존재 여부
    }
}