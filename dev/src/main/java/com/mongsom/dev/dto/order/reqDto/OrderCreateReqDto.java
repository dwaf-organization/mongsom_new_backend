package com.mongsom.dev.dto.order.reqDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateReqDto {
    
    @NotNull(message = "사용자 코드는 필수입니다.")
    private Long userCode;
    
    @NotNull(message = "수령인명은 필수입니다.")
    private String receivedUserName;
    
    @NotNull(message = "수령인 전화번호는 필수입니다.")
    private String receivedUserPhone;
    
    private String receivedUserZipCode;
    
    @NotNull(message = "수령인 주소는 필수입니다.")
    private String receivedUserAddress;
    
    @NotNull(message = "받는 사람 상세주소는 필수입니다")
    private String receivedUserAddress2;
    
    private String message;
    
    @NotNull(message = "총 상품 가격은 필수입니다.")
    @Positive(message = "총 상품 가격은 0보다 커야 합니다.")
    private Integer totalPrice;
    
    @NotNull(message = "배송비는 필수입니다.")
    private Integer deliveryPrice;
    
    @NotNull(message = "총 할인금액은 필수입니다")
    private Integer totalDiscountPrice;
    
    @NotNull(message = "최종 결제 금액은 필수입니다.")
    @Positive(message = "최종 결제 금액은 0보다 커야 합니다.")
    private Integer finalPrice;
    
    private Integer usedMileage = 0; // 사용 마일리지
    
    @NotNull(message = "결제 방식은 필수입니다.")
    private String paymentType; // CARD, BANK_TRANSFER
    
    @Valid
    @NotNull(message = "주문 상품 목록은 필수입니다.")
    private List<OrderDetailDto> orderDetails;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetailDto {
        
        @NotNull(message = "상품 ID는 필수입니다.")
        private Integer productId;
        
        private Integer option1; // 첫 번째 옵션 (NULL 허용)
        
        private Integer option2; // 두 번째 옵션 (NULL 허용)
        
        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 1 이상이어야 합니다.")
        private Integer quantity;
        
        @NotNull(message = "기본 가격은 필수입니다.")
        @Positive(message = "기본 가격은 0보다 커야 합니다.")
        private Integer basePrice;
        
        @Builder.Default
        private Integer optionPrice = 0; // 옵션 추가 가격
        
        // 옵션 존재 여부 체크
        public boolean hasOption1() {
            return option1 != null;
        }
        
        public boolean hasOption2() {
            return option2 != null;
        }
        
        public boolean hasAnyOptions() {
            return hasOption1() || hasOption2();
        }
    }
}