package com.mongsom.dev.dto.product.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListReqDto {
    
    private Integer premium; // null=전체, 1=프리미엄만
    
    @Builder.Default
    private String sortBy = "latest"; // latest(최신순), popular(인기순), review(리뷰많은순)
    
    @Builder.Default
    private Integer page = 0; // 페이지 번호 (0부터 시작)
    
    @Builder.Default
    private Integer size = 9; // 페이지 크기 (기본 9개)
    
    // 정렬 타입 검증
    public boolean isValidSortBy() {
        return "latest".equals(sortBy) || "popular".equals(sortBy) || "review".equals(sortBy);
    }
    
    // 프리미엄 필터 여부 확인
    public boolean isPremiumFilter() {
        return premium != null && premium == 1;
    }
    
    // 전체 상품 조회 여부 확인
    public boolean isAllProducts() {
        return premium == null;
    }
}