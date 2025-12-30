package com.mongsom.dev.dto.cart.respDto;

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
public class CartRespDto {
    
    private List<CartItemDto> cartItems;
    private CartSummary cartSummary;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDto {
        private Integer cartId;
        private Long userCode;
        private Integer productId;
        private String productName;
        private Integer basePrice;
        private Integer discountPrice;
        private Integer combinationId;
        private List<OptionInfo> selectedOptions; // 선택된 옵션 정보
        private Integer optionPrice; // 옵션 추가 가격
        private Integer unitPrice; // 개당 최종 가격 (basePrice + optionPrice)
        private Integer quantity;
        private Integer totalPrice; // 라인 총 가격 (unitPrice * quantity)
        private Integer checkStatus;
        private String mainImageUrl;
        private LocalDateTime createdAt;
        
        // 상태 체크 메서드
        public boolean isChecked() {
            return checkStatus == 1;
        }
        
        public boolean hasOptions() {
            return combinationId != null && selectedOptions != null && !selectedOptions.isEmpty();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionInfo {
        private String optionTypeName; // "용량", "색상"
        private String optionValueName; // "350ml", "블랙"
        private Integer priceAdjustment; // 추가/차감 가격
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartSummary {
        private Integer totalItems; // 총 상품 수 (체크된 것만)
        private Integer totalQuantity; // 총 수량 (체크된 것만)
        private Integer totalPrice; // 총 금액 (체크된 것만)
        private Integer deliveryPrice; // 배송비
        private Integer finalPrice; // 최종 금액 (총 금액 + 배송비)
        
        public static CartSummary from(List<CartItemDto> cartItems) {
            List<CartItemDto> checkedItems = cartItems.stream()
                    .filter(CartItemDto::isChecked)
                    .toList();
            
            int totalItems = checkedItems.size();
            int totalQuantity = checkedItems.stream()
                    .mapToInt(CartItemDto::getQuantity)
                    .sum();
            int totalPrice = checkedItems.stream()
                    .mapToInt(CartItemDto::getTotalPrice)
                    .sum();
            
            // 배송비 계산 (예: 5만원 이상 무료배송)
            int deliveryPrice = totalPrice >= 50000 ? 0 : 3000;
            int finalPrice = totalPrice + deliveryPrice;
            
            return CartSummary.builder()
                    .totalItems(totalItems)
                    .totalQuantity(totalQuantity)
                    .totalPrice(totalPrice)
                    .deliveryPrice(deliveryPrice)
                    .finalPrice(finalPrice)
                    .build();
        }
    }
    
    public static CartRespDto from(List<CartItemDto> cartItems) {
        CartSummary summary = CartSummary.from(cartItems);
        
        return CartRespDto.builder()
                .cartItems(cartItems)
                .cartSummary(summary)
                .build();
    }
}