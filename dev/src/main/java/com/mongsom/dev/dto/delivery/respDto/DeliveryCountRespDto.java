package com.mongsom.dev.dto.delivery.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCountRespDto {
    
    private Integer paymentCompleted; // 결제완료
    private Integer preparing;        // 상품준비중
    private Integer shipping;         // 배송중
    private Integer delivered;        // 배송완료
    
    // 정적 팩토리 메서드
    public static DeliveryCountRespDto of(Integer paymentCompleted, Integer preparing, 
                                         Integer shipping, Integer delivered) {
        return DeliveryCountRespDto.builder()
                .paymentCompleted(paymentCompleted)
                .preparing(preparing)
                .shipping(shipping)
                .delivered(delivered)
                .build();
    }
    
    // 모든 건수가 0인 경우
    public static DeliveryCountRespDto empty() {
        return DeliveryCountRespDto.builder()
                .paymentCompleted(0)
                .preparing(0)
                .shipping(0)
                .delivered(0)
                .build();
    }
}