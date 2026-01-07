package com.mongsom.dev.dto.admin.order.respDto;

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
public class AdminOrderListRespDto {
    
    private List<AdminOrderItemDto> orders;
    private PaginationDto pagination;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminOrderItemDto {
        private LocalDateTime paymentAt;     // 결제일시
        private Integer orderId;             // 주문 ID
        private String orderNum;             // 주문번호
        private Long userCode;			 // 유저코드
        private String orderUser;            // 주문자 (user_mst.name)
        
        // 상품정보
        private ProductInfoDto productInfo;
        
        private Integer finalPrice;          // 최종 결제금액
        private String paymentStatus;        // 결제상태 (payments 테이블)
        private String deliveryStatus;       // 배송상태 (order_item 테이블)
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfoDto {
        private String productName;          // 상품명 (외 N개 포함)
        private String productImgUrl;        // 대표 상품 이미지
        private String optionSummary;        // 옵션 요약 (500ml, 블랙)
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