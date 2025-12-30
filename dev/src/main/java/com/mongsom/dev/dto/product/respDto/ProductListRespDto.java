package com.mongsom.dev.dto.product.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListRespDto {
    
    private List<ProductItemDto> products;
    private PageInfo pageInfo;
    private FilterInfo filterInfo;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductItemDto {
        private Integer productId;
        private String name;
        private Integer basePrice;
        private Integer discountPrice;
        private Integer discountPer;
        private Integer premium; // 0=일반, 1=프리미엄
        private String mainImageUrl; // 대표 이미지
        private Integer reviewCount; // 리뷰 개수
        private Integer orderCount; // 주문 개수 (인기도)
        
        // 할인율이 있는지 확인
        public boolean hasDiscount() {
            return discountPer != null && discountPer > 0;
        }
        
        // 프리미엄 상품인지 확인
        public boolean isPremiumProduct() {
            return premium != null && premium == 1;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private Integer currentPage;
        private Integer totalPages;
        private Long totalElements;
        private Integer size;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterInfo {
        private Integer premium; // 현재 프리미엄 필터
        private String sortBy; // 현재 정렬 방식
        private String sortByName; // 정렬 방식 한글명
        
        public static FilterInfo from(Integer premium, String sortBy) {
            String sortByName = getSortByName(sortBy);
            return FilterInfo.builder()
                    .premium(premium)
                    .sortBy(sortBy)
                    .sortByName(sortByName)
                    .build();
        }
        
        private static String getSortByName(String sortBy) {
            switch (sortBy) {
                case "latest": return "최신순";
                case "popular": return "인기순";
                case "review": return "리뷰많은순";
                default: return "최신순";
            }
        }
    }
    
    public static ProductListRespDto from(List<ProductItemDto> products, 
                                         org.springframework.data.domain.Page<?> page,
                                         Integer premium, String sortBy) {
        
        PageInfo pageInfo = PageInfo.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
        
        FilterInfo filterInfo = FilterInfo.from(premium, sortBy);
        
        return ProductListRespDto.builder()
                .products(products)
                .pageInfo(pageInfo)
                .filterInfo(filterInfo)
                .build();
    }
}