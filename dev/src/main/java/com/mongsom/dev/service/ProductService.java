package com.mongsom.dev.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.product.reqDto.ProductListReqDto;
import com.mongsom.dev.dto.product.respDto.ProductDetailRespDto;
import com.mongsom.dev.dto.product.respDto.ProductListRespDto;
import com.mongsom.dev.dto.product.respDto.ProductReviewRespDto;
import com.mongsom.dev.entity.Product;
import com.mongsom.dev.entity.ProductImg;
import com.mongsom.dev.entity.ProductOptionType;
import com.mongsom.dev.entity.ProductOptionValue;
import com.mongsom.dev.entity.ReviewImg;
import com.mongsom.dev.entity.UserReview;
import com.mongsom.dev.repository.OrderDetailRepository;
import com.mongsom.dev.repository.ProductImgRepository;
import com.mongsom.dev.repository.ProductOptionTypeRepository;
import com.mongsom.dev.repository.ProductRepository;
import com.mongsom.dev.repository.ReviewImgRepository;
import com.mongsom.dev.repository.UserReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final UserReviewRepository userReviewRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final ProductImgRepository productImgRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductOptionTypeRepository productOptionTypeRepository;
    
    /**
     * 상품 목록 조회 (전체/프리미엄, 정렬별)
     */
    public RespDto<ProductListRespDto> getProductList(ProductListReqDto reqDto) {
        try {
            log.info("상품 목록 조회 시작 - premium: {}, sortBy: {}, page: {}, size: {}", 
                    reqDto.getPremium(), reqDto.getSortBy(), reqDto.getPage(), reqDto.getSize());
            
            // 1. 페이징 객체 생성
            Pageable pageable = PageRequest.of(reqDto.getPage(), reqDto.getSize());
            
            // 2. 조건에 따른 상품 조회
            Page<Product> productPage = getProductPageByCondition(reqDto, pageable);
            
            // 3. DTO 변환
            List<ProductListRespDto.ProductItemDto> productItems = productPage.getContent().stream()
                    .map(this::convertToProductItemDto)
                    .collect(Collectors.toList());
            
            // 4. 응답 생성
            ProductListRespDto responseData = ProductListRespDto.from(
                    productItems, 
                    productPage, 
                    reqDto.getPremium(), 
                    reqDto.getSortBy()
            );
            
            log.info("상품 목록 조회 완료 - 조회된 상품 수: {}, 총 개수: {}", 
                    productItems.size(), productPage.getTotalElements());
            
            return RespDto.<ProductListRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("상품 목록 조회 실패 - premium: {}, sortBy: {}", 
                    reqDto.getPremium(), reqDto.getSortBy(), e);
            
            return RespDto.<ProductListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }

    /**
     * 조건에 따른 상품 페이지 조회
     */
    private Page<Product> getProductPageByCondition(ProductListReqDto reqDto, Pageable pageable) {
        boolean isPremiumFilter = reqDto.isPremiumFilter();
        String sortBy = reqDto.getSortBy();
        
        if (isPremiumFilter) {
            // 프리미엄 상품 조회
            switch (sortBy) {
                case "latest":
                    return productRepository.findByPremiumAndDeleteStatusAndIsAvailableOrderByCreatedAtDesc(1, 0, 1, pageable);
                case "popular":
                    return productRepository.findByPremiumOrderByPopularityDesc(1, pageable);
                case "review":
                    return productRepository.findByPremiumOrderByReviewCountDesc(1, pageable);
                default:
                    return productRepository.findByPremiumAndDeleteStatusAndIsAvailableOrderByCreatedAtDesc(1, 0, 1, pageable);
            }
        } else {
            // 전체 상품 조회
            switch (sortBy) {
                case "latest":
                    return productRepository.findByDeleteStatusAndIsAvailableOrderByCreatedAtDesc(0, 1, pageable);
                case "popular":
                    return productRepository.findAllOrderByPopularityDesc(pageable);
                case "review":
                    return productRepository.findAllOrderByReviewCountDesc(pageable);
                default:
                    return productRepository.findByDeleteStatusAndIsAvailableOrderByCreatedAtDesc(0, 1, pageable);
            }
        }
    }

    /**
     * Product 엔티티를 ProductItemDto로 변환
     */
    private ProductListRespDto.ProductItemDto convertToProductItemDto(Product product) {
        // 대표 이미지 조회 (첫 번째 이미지)
        String mainImageUrl = null;
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            mainImageUrl = product.getProductImages().get(0).getProductImgUrl();
        }
        
        // 리뷰 개수 조회 (별도 쿼리 또는 연관관계로)
        Integer reviewCount = getReviewCount(product.getProductId());
        
        // 주문 개수 조회 (별도 쿼리 또는 연관관계로)  
        Integer orderCount = getOrderCount(product.getProductId());
        
        return ProductListRespDto.ProductItemDto.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .basePrice(product.getBasePrice())
                .salesMargin(product.getSalesMargin())
                .discountPrice(product.getDiscountPrice())
                .discountPer(product.getDiscountPer())
                .premium(product.getPremium())
                .stockStatus(product.getStockStatus())
                .mainImageUrl(mainImageUrl)
                .reviewCount(reviewCount)
                .orderCount(orderCount)
                .build();
    }

    /**
     * 상품의 리뷰 개수 조회
     */
    private Integer getReviewCount(Integer productId) {
        try {
            return Math.toIntExact(userReviewRepository.countByProductId(productId));
        } catch (Exception e) {
            log.warn("리뷰 개수 조회 실패 - productId: {}", productId);
            return 0;
        }
    }

    /**
     * 상품의 주문 개수 조회  
     */
    private Integer getOrderCount(Integer productId) {
        try {
            return Math.toIntExact(orderDetailRepository.countByProductIdAndOrderStatus(productId, 0));
        } catch (Exception e) {
            log.warn("주문 개수 조회 실패 - productId: {}", productId);
            return 0;
        }
    }

    /**
     * 상품 상세 조회 (수정된 버전)
     */
    public RespDto<ProductDetailRespDto> getProductDetail(Integer productId) {
        try {
            log.info("상품 상세 조회 시작 - productId: {}", productId);
            
            // 1. 기본 상품 조회 (상태 체크용)
            Optional<Product> productOpt = productRepository.findByIdOnly(productId);
            if (productOpt.isEmpty()) {
                log.warn("존재하지 않는 상품 - productId: {}", productId);
                return RespDto.<ProductDetailRespDto>builder()
                        .code(-1)
                        .data(ProductDetailRespDto.failure("존재하지 않는 상품입니다."))
                        .build();
            }
            
            Product basicProduct = productOpt.get();
            
            // 2. 삭제된 상품 체크
            if (basicProduct.getDeleteStatus() != null && basicProduct.getDeleteStatus() == 1) {
                log.warn("삭제된 상품 - productId: {}", productId);
                return RespDto.<ProductDetailRespDto>builder()
                        .code(-1)
                        .data(ProductDetailRespDto.failure("삭제된 상품입니다."))
                        .build();
            }
            
            // 3. 판매 중단된 상품 체크
            if (basicProduct.getIsAvailable() != null && basicProduct.getIsAvailable() == 0) {
                log.warn("판매 중단된 상품 - productId: {}", productId);
                return RespDto.<ProductDetailRespDto>builder()
                        .code(-1)
                        .data(ProductDetailRespDto.failure("판매가 중단된 상품입니다."))
                        .build();
            }
            
            // 4. 상품 + 이미지 조회
            Optional<Product> productWithImagesOpt = productRepository.findByIdWithImages(productId);
            Product product = productWithImagesOpt.orElse(basicProduct);
            
            // 5. 옵션 타입들과 옵션 값들 조회 (개선된 방식)
            List<ProductOptionType> optionTypes = productOptionTypeRepository.findByProductIdWithValues(productId);
            
            // 6. DTO 변환
            ProductDetailRespDto productDetail = convertToProductDetailDto(product, optionTypes);
            
            log.info("상품 상세 조회 완료 - productId: {}, 상품명: {}, 옵션타입 수: {}", 
                    productId, product.getName(), optionTypes.size());
            
            return RespDto.<ProductDetailRespDto>builder()
                    .code(1)
                    .data(productDetail)
                    .build();
            
        } catch (Exception e) {
            log.error("상품 상세 조회 실패 - productId: {}", productId, e);
            
            return RespDto.<ProductDetailRespDto>builder()
                    .code(-1)
                    .data(ProductDetailRespDto.failure("상품 조회 중 오류가 발생했습니다."))
                    .build();
        }
    }

    /**
     * Product 엔티티를 ProductDetailRespDto로 변환 (수정된 버전)
     */
    private ProductDetailRespDto convertToProductDetailDto(Product product, List<ProductOptionType> optionTypes) {
        // 1. 이미지 목록 변환
        List<ProductDetailRespDto.ProductImageDto> imageDtos = new ArrayList<>();
        if (product.getProductImages() != null) {
            imageDtos = product.getProductImages().stream()
                    .map(img -> ProductDetailRespDto.ProductImageDto.builder()
                            .productImgId(img.getProductImgId())
                            .productImgUrl(img.getProductImgUrl())
                            .build())
                    .collect(Collectors.toList());
        }
        
        // 2. 옵션 타입 목록 변환
        List<ProductDetailRespDto.OptionTypeDto> optionTypeDtos = optionTypes.stream()
                .map(optionType -> {
                    // 옵션 값 목록 변환 (이미 조회된 데이터 사용)
                    List<ProductDetailRespDto.OptionValueDto> optionValueDtos = new ArrayList<>();
                    if (optionType.getOptionValues() != null) {
                        optionValueDtos = optionType.getOptionValues().stream()
                                .filter(optionValue -> optionValue.getIsDeleted() == null || optionValue.getIsDeleted() == 0)
                                .sorted(Comparator.comparing(ProductOptionValue::getSortOrder))
                                .map(optionValue -> ProductDetailRespDto.OptionValueDto.builder()
                                        .optionValueId(optionValue.getOptionValueId())
                                        .valueName(optionValue.getValueName())
                                        .priceAdjustment(optionValue.getPriceAdjustment())
                                        .stockStatus(optionValue.getStockStatus())
                                        .sortOrder(optionValue.getSortOrder())
                                        .build())
                                .collect(Collectors.toList());
                    }
                    
                    return ProductDetailRespDto.OptionTypeDto.builder()
                            .optionTypeId(optionType.getOptionTypeId())
                            .typeName(optionType.getTypeName())
                            .isRequired(optionType.getIsRequired())
                            .sortOrder(optionType.getSortOrder())
                            .optionValues(optionValueDtos)
                            .build();
                })
                .collect(Collectors.toList());
        
        // 3. 상품 기본 정보 설정
        return ProductDetailRespDto.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .contents(product.getContents())
                .premium(product.getPremium())
                .basePrice(product.getBasePrice())
                .salesMargin(product.getSalesMargin())
                .discountPer(product.getDiscountPer())
                .discountPrice(product.getDiscountPrice())
                .deliveryPrice(product.getDeliveryPrice())
                .stockStatus(product.getStockStatus())
                .isAvailable(product.getIsAvailable())
                .deleteStatus(product.getDeleteStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .productImages(imageDtos)
                .optionTypes(optionTypeDtos)
                .build();
    }
    
    //상품별리뷰조회
	@Transactional(readOnly = true)
	public RespDto<ProductReviewRespDto> getProductReviews(Integer productCode, Pageable pageable) {
	    try {
	        // 상품 존재 여부 확인
	        Optional<Product> productOpt = productRepository.findById(productCode);
	        if (productOpt.isEmpty()) {
	            return RespDto.<ProductReviewRespDto>builder()
	                    .code(-1)
	                    .data(null)
	                    .build();
	        }
	        
	        // 상품 리뷰 조회 (페이징)
	        Page<UserReview> reviewPage = userReviewRepository.findByProductIdOrderByCreatedAtDesc(productCode, pageable);
	        
	        if (reviewPage.isEmpty()) {
	            return RespDto.<ProductReviewRespDto>builder()
	                    .code(1)
	                    .data(ProductReviewRespDto.builder()
	                            .items(List.of())
	                            .pagination(ProductReviewRespDto.PaginationDto.from(reviewPage))
	                            .build())
	                    .build();
	        }
	        
	        // 리뷰 ID 목록 추출
	        List<Integer> reviewIds = reviewPage.getContent().stream()
	                .map(UserReview::getReviewId)
	                .collect(Collectors.toList());
	        
	        // 리뷰 이미지들 조회 (한 번에 조회하여 N+1 문제 해결)
	        List<ReviewImg> reviewImgs = reviewImgRepository.findByReviewIdInOrderByReviewIdAndCreatedAt(reviewIds);
	        
	        // 리뷰 ID별로 이미지 그룹화
	        Map<Integer, List<String>> reviewImgMap = reviewImgs.stream()
	                .collect(Collectors.groupingBy(
	                        ReviewImg::getReviewId,
	                        Collectors.mapping(ReviewImg::getReviewImgUrl, Collectors.toList())
	                ));
	        
	        // ReviewItemDto 리스트 생성
	        List<ProductReviewRespDto.ReviewItemDto> reviewItems = reviewPage.getContent().stream()
	                .map(review -> {
	                    // 해당 리뷰의 이미지 URL 리스트
	                    List<String> reviewImgUrls = reviewImgMap.getOrDefault(review.getReviewId(), List.of());
	                    
	                    // 사용자 이름 가져오기
	                    String userName = review.getUser().getName();
	                    
	                    return ProductReviewRespDto.ReviewItemDto.from(review, userName, reviewImgUrls);
	                })
	                .collect(Collectors.toList());
	        
	        ProductReviewRespDto productReviewRespDto = ProductReviewRespDto.from(reviewItems, reviewPage);
	        
	        //상품 리뷰 조회 성공
	        return RespDto.<ProductReviewRespDto>builder()
	                .code(1)
	                .data(productReviewRespDto)
	                .build();
	                
	    } catch (Exception e) {
	    	//상품 리뷰 조회 실패
	        return RespDto.<ProductReviewRespDto>builder()
	                .code(-1)
	                .data(null)
	                .build();
	    }
	}
	
	
    /**
     * 상품 목록에 대한 이미지 맵을 생성하는 헬퍼 메서드
     * N+1 문제를 해결하기 위해 배치로 이미지를 조회
     */
    private Map<Integer, List<String>> getProductImagesMap(List<Product> products) {
        if (products.isEmpty()) {
            return Map.of();
        }
        
        // 상품 ID 목록 추출
        List<Integer> productIds = products.stream()
                .map(Product::getProductId)
                .collect(Collectors.toList());
        
        // 상품 이미지들 배치 조회 (N+1 문제 해결)
        List<ProductImg> productImgs = productImgRepository.findByProductIdInOrderByProductIdAndCreatedAt(productIds);
        
        // 상품 ID별로 이미지 URL 그룹화
        return productImgs.stream()
                .collect(Collectors.groupingBy(
                        img -> img.getProduct().getProductId(),
                        Collectors.mapping(ProductImg::getProductImgUrl, Collectors.toList())
                ));
    }
}