package com.mongsom.dev.dto.admin.change.respDto;

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
public class AdminChangeDetailRespDto {
    
    private ChangeInfo changeInfo;           // 교환/반품 신청 정보
    private OrderInfo orderInfo;             // 주문 기본 정보
    private PaymentInfo paymentInfo;         // 결제 정보
    private UserInfo userInfo;               // 주문자 정보
    private DeliveryInfo deliveryInfo;       // 배송 정보
    private ProductInfo productInfo;         // 상품 상세 정보
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeInfo {
        private Integer changeId;            // 교환/반품 ID
        private String changeType;           // 교환, 반품
        private String changeStatus;         // 교환신청, 교환승인 등
        private String reason;               // 교환반품 사유
        private String refundBank;           // 반품시 은행명
        private String refundAccount;        // 반품시 계좌번호
        private LocalDateTime requestedAt;   // 교환반품 요청일
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderInfo {
        private Integer orderId;             // 주문 ID
        private String orderNum;             // 주문번호
        private LocalDateTime orderCreatedAt; // 주문일시
        private LocalDateTime paymentAt;     // 결제일시
        private String deliveryStatus;       // 배송상태
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private Integer totalPrice;          // 상품 총액
        private Integer deliveryPrice;       // 배송비
        private Integer finalPrice;          // 최종 결제금액
        private Integer usedMileage;         // 사용 마일리지
        private String paymentMethod;        // 결제 방법
        private String paymentStatus;        // 결제 상태
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userCode;               // 사용자 코드
        private String userName;             // 사용자 이름
        private String userPhone;            // 사용자 전화번호
        private String userEmail;            // 사용자 이메일
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryInfo {
        private String receivedUserName;     // 받는 사람 이름
        private String receivedUserPhone;    // 받는 사람 전화번호
        private String receivedUserZipCode;  // 우편번호
        private String receivedUserAddress;  // 주소
        private String receivedUserAddress2; // 상세주소
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Integer orderDetailId;       // 주문 상세 ID
        private Integer productId;           // 상품 ID
        private String productName;          // 상품명
        private List<String> productImgUrls; // 상품 이미지 목록
        
        // 옵션 정보
        private Integer option1;             // 옵션1 ID
        private Integer option2;             // 옵션2 ID
        private String option1Name;          // 옵션1 이름
        private String option2Name;          // 옵션2 이름
        private String optionComb;           // 옵션 조합
        
        // 수량 및 가격 정보
        private Integer quantity;            // 수량
        private Integer lineTotalPrice;      // 라인 총액
        private Integer orderStatus;         // 주문 상태 (0=주문 1=주문취소 2=교환 3=반품)
    }
}