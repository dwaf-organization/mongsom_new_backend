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
public class MyOrderDetailRespDto {
    
    private OrderInfo orderInfo;           // 주문 정보
    private PaymentInfo paymentInfo;       // 결제 정보
    private DeliveryInfo deliveryInfo;     // 배송 정보
    private List<OrderItemDetail> orderItems; // 주문 상품 목록
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderInfo {
        private Integer orderId;           // 주문 ID
        private String orderNum;           // 주문 번호
        private LocalDateTime orderCreatedAt; // 주문일시
        private LocalDateTime paymentAt;   // 결제일시
        private String deliveryStatus;     // 배송상태
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private Integer totalPrice;        // 상품 총액
        private Integer deliveryPrice;     // 배송비
        private Integer totalDiscountPrice; // 총 할인금액
        private Integer finalPrice;        // 최종 결제금액
        private Integer usedMileage;       // 사용 마일리지
        private String deliveryStatusReason; // 결제수단 (무통장입금, 일반결제)
        
        // payments 테이블에서 가져오는 정보
        private String paymentMethod;      // 결제 방법 (카드, 계좌이체 등)
        private Integer paymentAmount;     // 결제 금액
        private String paymentStatus;      // 결제 상태 (대기중, 결제완료, 결제실패, 결제취소)
        private String pgProvider;         // PG사 (토스페이먼츠, KG이니시스 등)
        private LocalDateTime paymentCreatedAt;  // 결제 생성일시
        private LocalDateTime paymentUpdatedAt;  // 결제 수정일시
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryInfo {
        private String receivedUserName;   // 받는 사람 이름
        private String receivedUserPhone;  // 받는 사람 전화번호
        private String receivedUserZipCode; // 우편번호
        private String receivedUserAddress; // 주소
        private String receivedUserAddress2; // 상세주소
        private String message;            // 배송 메시지
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDetail {
        private Integer orderDetailId;     // 주문 상세 ID
        private Integer productId;         // 상품 ID
        private String productName;        // 상품명
        private Integer orderStatus;	   // 주문상태(0=주문 1=주문취소 2=교환 3=반품)
        private String changeStatus;       // 교환/반품 상태 (orderStatus가 2,3일 때만) ← 추가
        private String productImgUrl;      // 대표 상품 이미지
        
        // 옵션 정보
        private Integer option1;           // 옵션1 ID
        private Integer option2;           // 옵션2 ID
        private String option1Name;       // 옵션1 이름
        private String option2Name;       // 옵션2 이름
        
        // 가격 및 수량 정보
        private Integer quantity;          // 수량
        private Integer basePrice;         // 기본 가격
        private Integer optionPrice;       // 옵션 가격
        private Integer lineTotalPrice;    // 라인 총액 (수량 * (기본가격 + 옵션가격))
    }
}