package com.mongsom.dev.dto.order.respDto;

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
public class MyOrderListRespDto {
    
    private List<MyOrderItemDto> orders;
    private PaginationDto pagination;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyOrderItemDto {
        private Integer orderId;           // 주문 ID
        private String orderNum;           // 주문 번호
        private LocalDateTime paymentAt;   // 결제일시
        
        // 대표 상품 정보 (첫 번째 상품)
        private Integer productId;         // 상품 ID
        private String productName;        // 상품명 (외 N개 포함)
        private Integer option1;           // 옵션1 ID
        private Integer option2;           // 옵션2 ID
        private String option1Name;       // 옵션1 이름
        private String option2Name;       // 옵션2 이름
        private Integer quantity;          // 수량 (첫 번째 상품)
        
        // 주문 정보
        private Integer finalPrice;        // 최종 결제금액
        private Integer deliveryPrice;     // 배송비
        private String deliveryStatus;     // 배송상태
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