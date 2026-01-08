package com.mongsom.dev.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.product.reqDto.ProductListReqDto;
import com.mongsom.dev.dto.product.respDto.ProductDetailRespDto;
import com.mongsom.dev.dto.product.respDto.ProductListRespDto;
import com.mongsom.dev.dto.product.respDto.ProductReviewRespDto;
import com.mongsom.dev.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * 상품 목록 조회 (전체/프리미엄, 정렬별)
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<ProductListRespDto>> getProductList(
            @RequestParam(value = "premium", required = false) Integer premium,
            @RequestParam(value = "sortBy", defaultValue = "latest") String sortBy,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "9") Integer size) {
        
        log.info("=== 상품 목록 조회 요청 ===");
        log.info("premium: {}, sortBy: {}, page: {}, size: {}", premium, sortBy, page, size);
        
        // 요청 DTO 생성
        ProductListReqDto reqDto = ProductListReqDto.builder()
                .premium(premium)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();
        
        // 정렬 타입 검증
        if (!reqDto.isValidSortBy()) {
            log.warn("잘못된 정렬 타입: {}", sortBy);
            return ResponseEntity.badRequest().body(
                RespDto.<ProductListRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build()
            );
        }
        
        RespDto<ProductListRespDto> response = productService.getProductList(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("상품 목록 조회 결과 - code: {}", response.getCode());
        if (response.getData() != null && response.getData().getProducts() != null) {
            log.info("조회된 상품 수: {}, 총 페이지: {}", 
                    response.getData().getProducts().size(),
                    response.getData().getPageInfo().getTotalPages());
        }
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 상품 상세 조회
     */
    @GetMapping("/{productId}")
    public ResponseEntity<RespDto<ProductDetailRespDto>> getProductDetail(
            @PathVariable("productId") Integer productId) {
        
        log.info("=== 상품 상세 조회 요청 ===");
        log.info("productId: {}", productId);
        
        RespDto<ProductDetailRespDto> response = productService.getProductDetail(productId);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("상품 상세 조회 결과 - code: {}", response.getCode());
        if (response.getData() != null && response.getCode() == 1) {
            log.info("상품명: {}, 이미지 수: {}, 옵션타입 수: {}", 
                    response.getData().getName(),
                    response.getData().getProductImages() != null ? response.getData().getProductImages().size() : 0,
                    response.getData().getOptionTypes() != null ? response.getData().getOptionTypes().size() : 0);
        }
        
        return ResponseEntity.status(status).body(response);
    }
    
    //상품별리뷰조회
    @GetMapping("/review/{productCode}/{page}")
    public ResponseEntity<RespDto<ProductReviewRespDto>> getProductReviews(
            @PathVariable("productCode") Integer productCode,
            @PathVariable("page") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "latest") String sortBy) {
        
        log.info("=== 상품별 리뷰 조회 요청 ===");
        log.info("productCode: {}, page: {}, size: {}, sortBy: {}", productCode, page, size, sortBy);
        
        if (page < 1) {
            page = 1;
        }
        
        // sortBy 유효성 검증
        if (!isValidSortBy(sortBy)) {
            log.warn("잘못된 정렬 타입: {}", sortBy);
            return ResponseEntity.badRequest().body(
                RespDto.<ProductReviewRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        Pageable pageable = PageRequest.of(page - 1, size);
        
        RespDto<ProductReviewRespDto> response = productService.getProductReviews(productCode, pageable, sortBy);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("상품별 리뷰 조회 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * sortBy 유효성 검증
     */
    private boolean isValidSortBy(String sortBy) {
        return "latest".equals(sortBy) || "recommend".equals(sortBy) || "rating".equals(sortBy);
    }
}