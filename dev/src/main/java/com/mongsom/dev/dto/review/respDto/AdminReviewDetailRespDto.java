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
public class AdminReviewDetailRespDto {
    private Integer reviewId;          // 리뷰 ID
    private Long userCode;             // 사용자 코드
    private String userName;           // 사용자명
    private Integer orderDetailId;     // 주문 상세 ID
    private Integer productId;         // 상품 ID
    private String productName;        // 상품명
    private Integer orderId;           // 주문 ID
    private String orderNum;           // 주문 번호
    private Integer reviewRating;      // 리뷰 평점
    private String reviewContent;      // 리뷰 내용 (전체)
    private Integer adminHidden;       // 숨김 상태
    private String hiddenStatus;       // 숨김 상태 텍스트
    private LocalDateTime createdAt;   // 리뷰 작성일
    private LocalDateTime updatedAt;   // 수정일
    
    // 연관 정보
    private List<String> reviewImageUrls; // 리뷰 이미지들
    private OrderDetailInfo orderDetail;  // 주문 상세 정보
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetailInfo {
        private Integer quantity;          // 주문 수량
        private Integer option1;           // 옵션1
        private Integer option2;           // 옵션2
        private String optionSummary;      // 옵션 요약
        private LocalDateTime orderDate;   // 주문일
        private LocalDateTime paymentDate; // 결제일
        private String deliveryStatus;     // 배송 상태
    }
}