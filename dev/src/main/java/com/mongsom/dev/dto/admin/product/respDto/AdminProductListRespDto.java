package com.mongsom.dev.dto.admin.product.respDto;

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
public class AdminProductListRespDto {
    
    private List<ProductSummaryDto> products;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer size;
    private Boolean hasNext;
    private Boolean hasPrevious;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSummaryDto {
        private Integer productId;
        private String name;
        private String contents;
        private Integer premium; // 0=일반, 1=프리미엄
        private Integer basePrice;
        private Integer salesMargin;
        private Integer discountPer;
        private Integer discountPrice;
        private Integer deliveryPrice;
        private Integer stockStatus; // 0=품절, 1=주문가능, 2=부분주문가능
        private Integer isAvailable; // 0=판매중단, 1=판매중
        private Integer deleteStatus; // 0=정상, 1=삭제
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // 추가 정보
        private String firstImageUrl; // 첫 번째 이미지 URL
        private Integer imageCount; // 전체 이미지 개수
        private Integer optionTypeCount; // 옵션 타입 개수
        private Integer totalOptionValueCount; // 전체 옵션 값 개수
        private List<String> optionTypeNames; // 옵션 타입명들
        
        // 상태 표시용 메서드들
        public String getPremiumDisplay() {
            return premium == 1 ? "프리미엄" : "일반";
        }
        
        public String getStockStatusDisplay() {
            switch (stockStatus) {
                case 0: return "품절";
                case 1: return "주문가능";
                case 2: return "부분주문가능";
                default: return "알 수 없음";
            }
        }
        
        public String getAvailabilityDisplay() {
            return isAvailable == 1 ? "판매중" : "판매중단";
        }
        
        public String getDeleteStatusDisplay() {
            return deleteStatus == 0 ? "정상" : "삭제";
        }
        
        // 품절 여부
        public boolean isOutOfStock() {
            return stockStatus == 0;
        }
        
        // 일시정지 여부
        public boolean isPaused() {
            return isAvailable == 0;
        }
        
        // 옵션이 있는 상품인지 확인
        public boolean hasOptions() {
            return optionTypeCount != null && optionTypeCount > 0;
        }
        
        // 할인가 계산
        public Integer getActualPrice() {
            if (discountPrice != null && discountPrice > 0) {
                return discountPrice;
            }
            if (discountPer != null && discountPer > 0 && basePrice != null) {
                return basePrice - (basePrice * discountPer / 100);
            }
            return basePrice;
        }
    }
    
    // 정적 팩토리 메서드
    public static AdminProductListRespDto of(List<ProductSummaryDto> products, 
                                             Integer currentPage, 
                                             Integer totalPages, 
                                             Long totalElements, 
                                             Integer size) {
        return AdminProductListRespDto.builder()
                .products(products)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .size(size)
                .hasNext(currentPage < totalPages - 1)
                .hasPrevious(currentPage > 0)
                .build();
    }
}