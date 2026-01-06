package com.mongsom.dev.dto.admin.delivery.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDeliveryUpdateReqDto {
    
    @NotEmpty(message = "배송정보 목록은 비어있을 수 없습니다.")
    @Valid
    private List<DeliveryUpdateItemDto> deliveryUpdates;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryUpdateItemDto {
        
        @NotNull(message = "주문 ID는 필수입니다.")
        private Integer orderId;
        
        @NotNull(message = "사용자 코드는 필수입니다.")
        private Long userCode;
        
        @NotBlank(message = "배송상태는 필수입니다.")
        @Size(max = 30, message = "배송상태는 30자를 초과할 수 없습니다.")
        private String deliveryStatus;
        
        @Size(max = 50, message = "택배사는 50자를 초과할 수 없습니다.")
        private String deliveryCom;
        
        @Size(max = 50, message = "송장번호는 50자를 초과할 수 없습니다.")
        private String invoiceNum;
    }
}