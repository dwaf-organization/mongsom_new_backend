package com.mongsom.dev.dto.delivery.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryInfoRespDto {
    
    private String deliveryCom;    // 택배사
    private String invoiceNum;     // 운송장번호
    
    // 정적 팩토리 메서드
    public static DeliveryInfoRespDto of(String deliveryCom, String invoiceNum) {
        return DeliveryInfoRespDto.builder()
                .deliveryCom(deliveryCom)
                .invoiceNum(invoiceNum)
                .build();
    }
    
    // 빈 정보인 경우
    public static DeliveryInfoRespDto empty() {
        return DeliveryInfoRespDto.builder()
                .deliveryCom(null)
                .invoiceNum(null)
                .build();
    }
}