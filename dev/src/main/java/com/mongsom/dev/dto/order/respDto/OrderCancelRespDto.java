package com.mongsom.dev.dto.order.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelRespDto {
    
    private Integer orderId;             // 취소된 주문 ID
    private String orderNum;             // 주문번호
    private String previousStatus;       // 이전 배송상태
    private LocalDateTime canceledAt;    // 취소일시
    private String message;              // 취소 완료 메시지
    
    // 삭제된 데이터 정보 (선택적)
    private Integer deletedOrderDetails; // 삭제된 주문상품 개수
    private Boolean paymentDeleted;      // 결제정보 삭제 여부
}