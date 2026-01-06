package com.mongsom.dev.dto.order.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRespDto {
    
    private String orderNum;        // 주문번호 (mongsom_12345)
    private Integer finalPrice;     // 최종 결제금액
    private Integer orderId;        // 주문 ID (선택적 - 필요하다면 추가)
    
    // 추가로 필요할 수 있는 정보들 (선택적)
//    private Integer totalPrice;     // 상품 총액
//    private Integer deliveryPrice;  // 배송비
//    private Integer usedMileage;    // 사용한 마일리지
//    private String deliveryStatus;  // 배송상태
}