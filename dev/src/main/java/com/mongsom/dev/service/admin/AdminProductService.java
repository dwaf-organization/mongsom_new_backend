package com.mongsom.dev.service.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongsom.dev.common.dto.PaginationDto;
import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.admin.product.reqDto.AdminProductRegistReqDto;
import com.mongsom.dev.dto.admin.product.reqDto.AdminProductUpdateReqDto;
import com.mongsom.dev.dto.admin.product.reqDto.ChangeApprovalReqDto;
import com.mongsom.dev.dto.admin.product.reqDto.ProductRegistReqDto;
import com.mongsom.dev.dto.admin.product.respDto.AdminProductDetailRespDto;
import com.mongsom.dev.dto.admin.product.respDto.AdminProductListRespDto;
import com.mongsom.dev.dto.admin.product.respDto.AdminProductRegistRespDto;
import com.mongsom.dev.dto.admin.product.respDto.AdminProductSelectRespDto;
import com.mongsom.dev.dto.admin.product.respDto.AdminProductUpdateRespDto;
import com.mongsom.dev.dto.admin.product.respDto.ChangeProductListRespDto;
import com.mongsom.dev.entity.ChangeItem;
import com.mongsom.dev.entity.OptionCombinationMapping;
import com.mongsom.dev.entity.OrderDetail;
import com.mongsom.dev.entity.OrderItem;
import com.mongsom.dev.entity.Product;
import com.mongsom.dev.entity.ProductImg;
import com.mongsom.dev.entity.ProductOptionCombination;
import com.mongsom.dev.entity.ProductOptionType;
import com.mongsom.dev.entity.ProductOptionValue;
import com.mongsom.dev.repository.CartRepository;
import com.mongsom.dev.repository.ChangeItemRepository;
import com.mongsom.dev.repository.OptionCombinationMappingRepository;
import com.mongsom.dev.repository.ProductImgRepository;
import com.mongsom.dev.repository.ProductOptionCombinationRepository;
import com.mongsom.dev.repository.ProductOptionTypeRepository;
import com.mongsom.dev.repository.ProductOptionValueRepository;
import com.mongsom.dev.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductService {
    
    private final ProductRepository productRepository;
    private final ProductOptionTypeRepository productOptionTypeRepository;
    private final ProductImgRepository productImgRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    private final ProductOptionCombinationRepository productOptionCombinationRepository;
    private final OptionCombinationMappingRepository optionCombinationMappingRepository;
    
    /**
     * 상품 등록
     */
    @Transactional
    public RespDto<AdminProductRegistRespDto> registProduct(AdminProductRegistReqDto reqDto) {
        try {
            log.info("=== 상품 등록 시작 ===");
            log.info("상품명: {}", reqDto.getName());
            
            // 1. 상품명 중복 검사
            if (productRepository.existsByName(reqDto.getName())) {
                log.warn("이미 존재하는 상품명: {}", reqDto.getName());
                return RespDto.<AdminProductRegistRespDto>builder()
                        .code(-1)
                        .data(AdminProductRegistRespDto.failure("이미 존재하는 상품명입니다."))
                        .build();
            }
            
            // 2. Product 엔티티 생성
            Product product = Product.builder()
                    .name(reqDto.getName())
                    .contents(reqDto.getContents())
                    .basePrice(reqDto.getBasePrice())
                    .premium(reqDto.getPremium())
                    .salesMargin(reqDto.getSalesMargin())
                    .discountPer(reqDto.getDiscountPer())
                    .discountPrice(reqDto.getDiscountPrice())
                    .deliveryPrice(reqDto.getDeliveryPrice())
                    .stockStatus(reqDto.getStockStatus())
                    .isAvailable(reqDto.getIsAvailable()) // 추가된 필드
                    .deleteStatus(0) // 기본값: 정상
                    .build();
            
            // 3. 상품 저장
            Product savedProduct = productRepository.save(product);
            Integer productId = savedProduct.getProductId();
            log.info("상품 저장 완료 - productId: {}", productId);
            
            // 4. 상품 이미지 등록
            if (reqDto.getProductImgUrls() != null && !reqDto.getProductImgUrls().isEmpty()) {
                log.info("상품 이미지 등록 시작 - 개수: {}", reqDto.getProductImgUrls().size());
                
                for (String imageUrl : reqDto.getProductImgUrls()) {
                    ProductImg productImg = ProductImg.builder()
                            .productId(productId) // productId 직접 설정
                            .productImgUrl(imageUrl)
                            .build();
                    
                    savedProduct.addProductImage(productImg);
                }
                log.info("상품 이미지 등록 완료");
            }
            
            // 5. 옵션 타입 및 값 등록 (수정됨)
            if (reqDto.getOptionTypes() != null && !reqDto.getOptionTypes().isEmpty()) {
                log.info("옵션 등록 시작 - 옵션타입 개수: {}", reqDto.getOptionTypes().size());
                
                for (AdminProductRegistReqDto.OptionTypeDto optionTypeDto : reqDto.getOptionTypes()) {
                    // 옵션 타입 생성
                    ProductOptionType optionType = ProductOptionType.builder()
                            .productId(productId) // productId 직접 설정
                            .typeName(optionTypeDto.getTypeName())
                            .isRequired(optionTypeDto.getIsRequired())
                            .sortOrder(optionTypeDto.getSortOrder())
                            .build();
                    
                    // 옵션 타입 저장 (먼저 저장해서 ID 생성)
                    ProductOptionType savedOptionType = productOptionTypeRepository.save(optionType);
                    savedProduct.addOptionType(savedOptionType);
                    
                    log.info("옵션타입 등록: {} (ID: {}, 필수: {})", 
                            savedOptionType.getTypeName(), 
                            savedOptionType.getOptionTypeId(),
                            savedOptionType.getIsRequired());
                    
                    // 옵션 값들 생성
                    if (optionTypeDto.getOptionValues() != null) {
                        for (AdminProductRegistReqDto.OptionValueDto optionValueDto : optionTypeDto.getOptionValues()) {
                            ProductOptionValue optionValue = ProductOptionValue.builder()
                                    .optionTypeId(savedOptionType.getOptionTypeId()) // 저장된 옵션타입 ID 사용
                                    .valueName(optionValueDto.getValueName())
                                    .priceAdjustment(optionValueDto.getPriceAdjustment())
                                    .stockStatus(optionValueDto.getStockStatus())
                                    .sortOrder(optionValueDto.getSortOrder())
                                    .build();
                            
                            savedOptionType.addOptionValue(optionValue);
                            log.info("  - 옵션값 등록: {} (가격조정: {}, 재고상태: {})", 
                                    optionValue.getValueName(), 
                                    optionValue.getPriceAdjustment(),
                                    optionValue.getStockStatus());
                        }
                    }
                }
                
                // 옵션 조합 생성 (옵션이 있는 경우만)
                generateOptionCombinations(savedProduct);
                log.info("옵션 조합 생성 완료");
                
                log.info("옵션 등록 완료");
            }
            
            // 6. 최종 저장 (Cascade로 모든 연관 엔티티 자동 저장)
            productRepository.save(savedProduct);
            log.info("=== 상품 등록 완료 - productId: {} ===", productId);
            
            return RespDto.<AdminProductRegistRespDto>builder()
                    .code(1)
                    .data(AdminProductRegistRespDto.success(productId))
                    .build();
                    
        } catch (Exception e) {
            log.error("상품 등록 실패 - name: {}, error: {}", reqDto.getName(), e.getMessage(), e);
            
            return RespDto.<AdminProductRegistRespDto>builder()
                    .code(-1)
                    .data(AdminProductRegistRespDto.failure("상품 등록 중 오류가 발생했습니다: " + e.getMessage()))
                    .build();
        }
    }
    
    // 재고 관련 로직 제거하고 간단하게 수정
    private void generateOptionCombinations(Product product) {
        List<ProductOptionType> optionTypes = productOptionTypeRepository
                .findByProductIdOrderBySortOrder(product.getProductId());
        
        if (optionTypes.isEmpty()) return;
        
        // 각 옵션 타입별 값들 조회 (삭제되지 않은 것만)
        List<List<ProductOptionValue>> allOptionValues = optionTypes.stream()
                .map(type -> productOptionValueRepository.findByOptionTypeIdOrderBySortOrder(type.getOptionTypeId()))
                .filter(values -> !values.isEmpty()) // 빈 옵션 타입 제외
                .collect(Collectors.toList());
        
        if (allOptionValues.isEmpty()) return;
        
        // 카테시안 곱으로 모든 조합 생성
        List<List<ProductOptionValue>> combinations = cartesianProduct(allOptionValues);
        
        // 조합들을 저장 (재고 관련 로직 제거)
        for (List<ProductOptionValue> combination : combinations) {
            String combinationKey = combination.stream()
                    .map(v -> v.getOptionValueId().toString())
                    .sorted()
                    .collect(Collectors.joining("-"));
            
            // 모든 옵션이 주문가능해야 조합도 주문가능
            Integer stockStatus = combination.stream()
                    .allMatch(v -> v.getStockStatus() == 1) ? 1 : 0;
            
            ProductOptionCombination combinationEntity = ProductOptionCombination.builder()
                    .productId(product.getProductId())
                    .combinationKey(combinationKey)
                    .stockStatus(stockStatus)
                    .isDeleted(0) // 기본값
                    .build();
            
            productOptionCombinationRepository.save(combinationEntity);
            
            // 조합-옵션값 매핑 저장
            for (ProductOptionValue value : combination) {
                OptionCombinationMapping mapping = OptionCombinationMapping.builder()
                        .combinationId(combinationEntity.getCombinationId())
                        .optionValueId(value.getOptionValueId())
                        .build();
                
                optionCombinationMappingRepository.save(mapping);
            }
        }
    }

    // 카테시안 곱 계산 헬퍼 메서드
    private List<List<ProductOptionValue>> cartesianProduct(List<List<ProductOptionValue>> lists) {
        if (lists.isEmpty()) {
            return Arrays.asList(Arrays.asList());
        }
        
        List<ProductOptionValue> head = lists.get(0);
        List<List<ProductOptionValue>> tail = cartesianProduct(lists.subList(1, lists.size()));
        
        List<List<ProductOptionValue>> result = new ArrayList<>();
        for (ProductOptionValue h : head) {
            for (List<ProductOptionValue> t : tail) {
                List<ProductOptionValue> combination = new ArrayList<>();
                combination.add(h);
                combination.addAll(t);
                result.add(combination);
            }
        }
        return result;
    }
    
    /**
     * 상품 상세 조회 (수정용)
     */
    @Transactional(readOnly = true)
    public RespDto<AdminProductDetailRespDto> getProductDetail(Integer productId) {
        try {
            log.info("=== 상품 상세 조회 시작 - productId: {} ===", productId);
            
            // 1. 상품 기본 정보 조회
            Product product = productRepository.findByIdOnly(productId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. productId: " + productId));
            
            // 2. 삭제된 상품인지 확인
            if (product.isDeleted()) {
                log.warn("삭제된 상품 조회 시도 - productId: {}", productId);
                return RespDto.<AdminProductDetailRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            log.info("상품 조회 완료 - name: {}", product.getName());
            
            // 2. 상품 이미지 정보 별도 조회
            List<ProductImg> productImgs = productImgRepository.findByProductId(productId);
            List<AdminProductDetailRespDto.ProductImageDto> productImages = productImgs.stream()
                    .map(img -> AdminProductDetailRespDto.ProductImageDto.from(
                            img.getProductImgId(), 
                            img.getProductImgUrl()))
                    .collect(Collectors.toList());
            
            log.info("상품 이미지 개수: {}", productImages.size());
            
            // 3. 옵션 타입 별도 조회
            List<ProductOptionType> optionTypes = productOptionTypeRepository.findByProductIdOrderBySortOrder(productId);
            
            // 4. 각 옵션 타입에 대해 옵션 값들을 조회
            List<AdminProductDetailRespDto.OptionTypeDto> optionTypeDtos = optionTypes.stream()
                    .map(optionType -> {
                        // 옵션 값들 조회
                        List<ProductOptionValue> optionValues = productOptionValueRepository
                                .findByOptionTypeIdOrderBySortOrder(optionType.getOptionTypeId());
                        
                        // OptionValueDto 생성
                        List<AdminProductDetailRespDto.OptionValueDto> optionValueDtos = optionValues.stream()
                                .map(AdminProductDetailRespDto.OptionValueDto::from)
                                .collect(Collectors.toList());
                        
                        // OptionTypeDto 생성
                        return AdminProductDetailRespDto.OptionTypeDto.builder()
                                .optionTypeId(optionType.getOptionTypeId())
                                .typeName(optionType.getTypeName())
                                .isRequired(optionType.getIsRequired())
                                .sortOrder(optionType.getSortOrder())
                                .optionValues(optionValueDtos)
                                .build();
                    })
                    .collect(Collectors.toList());
            
            log.info("옵션 타입 개수: {}", optionTypeDtos.size());
            
            // 5. 응답 생성
            AdminProductDetailRespDto response = AdminProductDetailRespDto.from(
                    product, productImages, optionTypeDtos);
            
            log.info("=== 상품 상세 조회 완료 - productId: {} ===", productId);
            
            return RespDto.<AdminProductDetailRespDto>builder()
                    .code(1)
                    .data(response)
                    .build();
                    
        } catch (IllegalArgumentException e) {
            log.warn("상품 상세 조회 실패 - productId: {}, error: {}", productId, e.getMessage());
            
            return RespDto.<AdminProductDetailRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
                    
        } catch (Exception e) {
            log.error("상품 상세 조회 실패 - productId: {}, error: {}", productId, e.getMessage(), e);
            
            return RespDto.<AdminProductDetailRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * 상품 수정
     */
    @Transactional
    public RespDto<AdminProductUpdateRespDto> updateProduct(Integer productId, AdminProductUpdateReqDto reqDto) {
        try {
            log.info("=== 상품 수정 시작 - productId: {} ===", productId);
            log.info("상품명: {}", reqDto.getName());
            
            // 1. 기존 상품 조회
            Product product = productRepository.findByIdOnly(productId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. productId: " + productId));
            
            // 2. 상품 기본 정보 수정
            product.updateProduct(
                reqDto.getName(),
                reqDto.getContents(), 
                reqDto.getBasePrice(),
                reqDto.getPremium(),
                reqDto.getSalesMargin(),
                reqDto.getDiscountPer(),
                reqDto.getDiscountPrice(),
                reqDto.getDeliveryPrice()
            );
            
            log.info("상품 기본 정보 수정 완료");
            
            AdminProductUpdateRespDto.UpdateSummary summary = AdminProductUpdateRespDto.UpdateSummary.builder()
                    .updatedImages(0)
                    .addedImages(0)
                    .deletedImages(0)
                    .updatedOptionTypes(0)
                    .addedOptionTypes(0)
                    .deletedOptionTypes(0)
                    .updatedOptionValues(0)
                    .addedOptionValues(0)
                    .deletedOptionValues(0)
                    .build();
            
            // 3. 이미지 수정
            if (reqDto.getProductImages() != null) {
                log.info("이미지 수정 시작 - 개수: {}", reqDto.getProductImages().size());
                
                for (AdminProductUpdateReqDto.ProductImageDto imageDto : reqDto.getProductImages()) {
                    if (imageDto.getIsDeleted()) {
                        // 삭제
                        if (imageDto.getProductImgId() != null) {
                            productImgRepository.deleteById(imageDto.getProductImgId());
                            summary.setDeletedImages(summary.getDeletedImages() + 1);
                            log.info("이미지 삭제 - ID: {}", imageDto.getProductImgId());
                        }
                    } else if (imageDto.getProductImgId() == null) {
                        // 신규 추가
                        ProductImg newImg = ProductImg.builder()
                                .productId(productId)
                                .productImgUrl(imageDto.getProductImgUrl())
                                .build();
                        productImgRepository.save(newImg);
                        summary.setAddedImages(summary.getAddedImages() + 1);
                        log.info("이미지 추가 - URL: {}", imageDto.getProductImgUrl());
                    } else {
                        // 기존 수정
                        ProductImg existingImg = productImgRepository.findById(imageDto.getProductImgId())
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지입니다."));
                        existingImg.updateImageUrl(imageDto.getProductImgUrl());
                        productImgRepository.save(existingImg);
                        summary.setUpdatedImages(summary.getUpdatedImages() + 1);
                        log.info("이미지 수정 - ID: {}", imageDto.getProductImgId());
                    }
                }
                log.info("이미지 수정 완료");
            }
            
            // 4. 옵션 수정
            if (reqDto.getOptionTypes() != null) {
                log.info("옵션 수정 시작 - 옵션타입 개수: {}", reqDto.getOptionTypes().size());
                
                for (AdminProductUpdateReqDto.OptionTypeDto optionTypeDto : reqDto.getOptionTypes()) {
                    if (optionTypeDto.getIsDeleted()) {
                        // 옵션 타입 삭제
                        if (optionTypeDto.getOptionTypeId() != null) {
                            productOptionTypeRepository.deleteById(optionTypeDto.getOptionTypeId());
                            summary.setDeletedOptionTypes(summary.getDeletedOptionTypes() + 1);
                            log.info("옵션타입 삭제 - ID: {}", optionTypeDto.getOptionTypeId());
                        }
                        continue;
                    }
                    
                    ProductOptionType optionType;
                    if (optionTypeDto.getOptionTypeId() == null) {
                        // 신규 옵션 타입 생성
                        optionType = ProductOptionType.builder()
                                .productId(productId)
                                .typeName(optionTypeDto.getTypeName())
                                .isRequired(optionTypeDto.getIsRequired())
                                .sortOrder(optionTypeDto.getSortOrder())
                                .build();
                        optionType = productOptionTypeRepository.save(optionType);
                        summary.setAddedOptionTypes(summary.getAddedOptionTypes() + 1);
                        log.info("옵션타입 추가 - 이름: {}", optionTypeDto.getTypeName());
                    } else {
                        // 기존 옵션 타입 수정
                        optionType = productOptionTypeRepository.findById(optionTypeDto.getOptionTypeId())
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션 타입입니다."));
                        optionType.updateTypeName(optionTypeDto.getTypeName());
                        optionType.updateSortOrder(optionTypeDto.getSortOrder());
                        optionType = productOptionTypeRepository.save(optionType);
                        summary.setUpdatedOptionTypes(summary.getUpdatedOptionTypes() + 1);
                        log.info("옵션타입 수정 - ID: {}", optionTypeDto.getOptionTypeId());
                    }
                    
                    // 5. 옵션 값 수정
                    if (optionTypeDto.getOptionValues() != null) {
                        for (AdminProductUpdateReqDto.OptionValueDto optionValueDto : optionTypeDto.getOptionValues()) {
                            if (optionValueDto.getIsDeleted()) {
                                // 옵션 값 삭제
                                if (optionValueDto.getOptionValueId() != null) {
                                    productOptionValueRepository.deleteById(optionValueDto.getOptionValueId());
                                    summary.setDeletedOptionValues(summary.getDeletedOptionValues() + 1);
                                    log.info("옵션값 삭제 - ID: {}", optionValueDto.getOptionValueId());
                                }
                            } else if (optionValueDto.getOptionValueId() == null) {
                                // 신규 옵션 값 생성
                                ProductOptionValue optionValue = ProductOptionValue.builder()
                                        .optionTypeId(optionType.getOptionTypeId())
                                        .valueName(optionValueDto.getValueName())
                                        .priceAdjustment(optionValueDto.getPriceAdjustment())
                                        .stockStatus(optionValueDto.getStockStatus())
                                        .sortOrder(optionValueDto.getSortOrder())
                                        .build();
                                productOptionValueRepository.save(optionValue);
                                summary.setAddedOptionValues(summary.getAddedOptionValues() + 1);
                                log.info("옵션값 추가 - 이름: {}", optionValueDto.getValueName());
                            } else {
                                // 기존 옵션 값 수정
                                ProductOptionValue optionValue = productOptionValueRepository.findById(optionValueDto.getOptionValueId())
                                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션 값입니다."));
                                optionValue.updateValueName(optionValueDto.getValueName());
                                optionValue.updatePriceAdjustment(optionValueDto.getPriceAdjustment());
                                productOptionValueRepository.save(optionValue);
                                summary.setUpdatedOptionValues(summary.getUpdatedOptionValues() + 1);
                                log.info("옵션값 수정 - ID: {}", optionValueDto.getOptionValueId());
                            }
                        }
                    }
                }
                log.info("옵션 수정 완료");
            }
            
            // 6. 최종 저장
            productRepository.save(product);
            log.info("=== 상품 수정 완료 - productId: {} ===", productId);
            
            return RespDto.<AdminProductUpdateRespDto>builder()
                    .code(1)
                    .data(AdminProductUpdateRespDto.success(productId, summary))
                    .build();
                    
        } catch (IllegalArgumentException e) {
            log.warn("상품 수정 실패 - productId: {}, error: {}", productId, e.getMessage());
            
            return RespDto.<AdminProductUpdateRespDto>builder()
                    .code(-1)
                    .data(AdminProductUpdateRespDto.failure(e.getMessage()))
                    .build();
                    
        } catch (Exception e) {
            log.error("상품 수정 실패 - productId: {}, error: {}", productId, e.getMessage(), e);
            
            return RespDto.<AdminProductUpdateRespDto>builder()
                    .code(-1)
                    .data(AdminProductUpdateRespDto.failure("상품 수정 중 오류가 발생했습니다: " + e.getMessage()))
                    .build();
        }
    }
    
    /**
     * 상품 목록 조회 (검색 조건 포함)
     */
    @Transactional(readOnly = true)
    public RespDto<AdminProductListRespDto> getProductList(Integer page, Integer size, 
                                                          String name, Integer premium, 
                                                          Integer outOfStock, Integer paused) {
        try {
            log.info("=== 관리자 상품 목록 조회 시작 ===");
            log.info("페이지: {} (크기: {})", page, size);
            log.info("검색조건 - 상품명: {}, 프리미엄: {}, 품절: {}, 일시정지: {}", name, premium, outOfStock, paused);
            
            // 페이징 설정
            Pageable pageable = PageRequest.of(page, size);
            
            // 상품 목록 조회
            Page<Object[]> productPage = productRepository.findProductsWithDetails(
                    name, premium, outOfStock, paused, pageable);
            
            log.info("조회된 상품 수: {} / 전체: {}", productPage.getContent().size(), productPage.getTotalElements());
            
            // DTO 변환
            List<AdminProductListRespDto.ProductSummaryDto> productSummaries = productPage.getContent().stream()
                    .map(this::convertToProductSummary)
                    .collect(Collectors.toList());
            
            // 각 상품의 옵션 타입명 조회 (N+1 해결을 위해 일괄 조회)
            for (AdminProductListRespDto.ProductSummaryDto product : productSummaries) {
                List<String> optionTypeNames = productRepository.findOptionTypeNamesByProductId(product.getProductId());
                product.setOptionTypeNames(optionTypeNames);
            }
            
            // 응답 생성
            AdminProductListRespDto response = AdminProductListRespDto.of(
                    productSummaries,
                    productPage.getNumber(),
                    productPage.getTotalPages(),
                    productPage.getTotalElements(),
                    productPage.getSize()
            );
            
            log.info("=== 상품 목록 조회 완료 ===");
            
            return RespDto.<AdminProductListRespDto>builder()
                    .code(1)
                    .data(response)
                    .build();
                    
        } catch (Exception e) {
            log.error("상품 목록 조회 실패 - error: {}", e.getMessage(), e);
            
            return RespDto.<AdminProductListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * Object[] 을 ProductSummaryDto로 변환
     */
    private AdminProductListRespDto.ProductSummaryDto convertToProductSummary(Object[] row) {
        // Native Query 결과를 매핑 (순서대로)
        // p.*, first_image_url, image_count, option_type_count, total_option_value_count
        
        return AdminProductListRespDto.ProductSummaryDto.builder()
                .productId((Integer) row[0])  // product_id
                .name((String) row[1])        // name
                .contents((String) row[2])    // contents
                .premium((Integer) row[3])    // premium
                .basePrice((Integer) row[4])  // base_price
                .salesMargin((Integer) row[5]) // sales_margin
                .discountPer((Integer) row[6]) // discount_per
                .discountPrice((Integer) row[7]) // discount_price
                .deliveryPrice((Integer) row[8]) // delivery_price
                .stockStatus((Integer) row[9])   // stock_status
                .isAvailable((Integer) row[10])  // is_available
                .deleteStatus((Integer) row[11]) // delete_status
                .createdAt(row[12] != null ? ((java.sql.Timestamp) row[12]).toLocalDateTime() : null)  // created_at
                .updatedAt(row[13] != null ? ((java.sql.Timestamp) row[13]).toLocalDateTime() : null)  // updated_at
                .firstImageUrl((String) row[14])     // first_image_url
                .imageCount(row[15] != null ? ((Number) row[15]).intValue() : 0)     // image_count
                .optionTypeCount(row[16] != null ? ((Number) row[16]).intValue() : 0) // option_type_count
                .totalOptionValueCount(row[17] != null ? ((Number) row[17]).intValue() : 0) // total_option_value_count
                .build();
    }

    /**
     * 상품 소프트 삭제 (delete_status = 1로 변경)
     */
    @Transactional
    public RespDto<Boolean> softDeleteProduct(Integer productId) {
        try {
            log.info("=== 상품 소프트 삭제 시작 - productId: {} ===", productId);
            
            // 1. 상품 존재 확인
            Product product = productRepository.findByIdOnly(productId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. productId: " + productId));
            
            // 2. 이미 삭제된 상품인지 확인
            if (product.getDeleteStatus() == 1) {
                log.warn("이미 삭제된 상품입니다 - productId: {}", productId);
                return RespDto.<Boolean>builder()
                        .code(-1)
                        .data(false)
                        .build();
            }
            
            // 3. delete_status를 1로 변경
            product.softDelete(); // Product 엔티티에 이 메서드 추가 필요
            productRepository.save(product);
            
            log.info("상품 소프트 삭제 완료 - productId: {}, name: {}", productId, product.getName());
            
            return RespDto.<Boolean>builder()
                    .code(1)
                    .data(true)
                    .build();
                    
        } catch (IllegalArgumentException e) {
            log.warn("상품 소프트 삭제 실패 - productId: {}, error: {}", productId, e.getMessage());
            
            return RespDto.<Boolean>builder()
                    .code(-1)
                    .data(false)
                    .build();
                    
        } catch (Exception e) {
            log.error("상품 소프트 삭제 실패 - productId: {}, error: {}", productId, e.getMessage(), e);
            
            return RespDto.<Boolean>builder()
                    .code(-1)
                    .data(false)
                    .build();
        }
    }
    
    @Transactional
    public RespDto<Boolean> deleteOptionType(Integer optionTypeId) {
        try {
            ProductOptionType optionType = productOptionTypeRepository.findById(optionTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션 타입입니다."));
            
            optionType.softDelete();
            
            // 해당 타입의 모든 옵션 값도 삭제
            List<ProductOptionValue> values = productOptionValueRepository.findByOptionTypeId(optionTypeId);
            values.forEach(ProductOptionValue::softDelete);
            
            // 관련 조합들도 재생성 필요
            regenerateProductCombinations(optionType.getProductId());
            
            return RespDto.<Boolean>builder().code(1).data(true).build();
        } catch (Exception e) {
            log.error("옵션 타입 삭제 실패: {}", e.getMessage());
            return RespDto.<Boolean>builder().code(-1).data(false).build();
        }
    }

    @Transactional
    public RespDto<Boolean> deleteOptionValue(Integer optionValueId) {
        try {
            ProductOptionValue optionValue = productOptionValueRepository.findById(optionValueId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션 값입니다."));
            
            optionValue.softDelete();
            
            // 관련 조합들 재생성
            ProductOptionType optionType = optionValue.getOptionType();
            regenerateProductCombinations(optionType.getProductId());
            
            return RespDto.<Boolean>builder().code(1).data(true).build();
        } catch (Exception e) {
            log.error("옵션 값 삭제 실패: {}", e.getMessage());
            return RespDto.<Boolean>builder().code(-1).data(false).build();
        }
    }

    private void regenerateProductCombinations(Integer productId) {
        // 기존 조합들 삭제
        productOptionCombinationRepository.deleteByProductId(productId);
        
        // 새로운 조합들 생성
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            generateOptionCombinations(product);
        }
    }
    
//    // 교환/반품 상품 목록 조회
//    public RespDto<ChangeProductListRespDto> getChangeProductList(Integer changeStatus, Integer page) {
//        try {
//            log.info("=== 교환/반품 상품 목록 조회 시작 - changeStatus: {}, page: {} ===", changeStatus, page);
//            
//            // 1. 페이징 설정 (사이즈 5 고정)
//            Pageable pageable = PageRequest.of(page - 1, 5);
//            
//            // 2. 교환/반품 항목 조회 (복합 조인 쿼리)
//            List<Object[]> changeProductRows = changeItemRepository.findChangeProductListByStatus(changeStatus, pageable);
//            
//            // 3. DTO 변환
//            List<ChangeProductListRespDto.ChangeItemDto> changeItemDtos = changeProductRows.stream()
//                    .map(this::convertToChangeItemDto)
//                    .collect(Collectors.toList());
//            
//            // 4. 전체 개수 조회 (페이징 정보용)
//            long totalElements = changeItemRepository.countByChangeStatus(changeStatus);
//            int totalPages = (int) Math.ceil((double) totalElements / 5);
//            boolean hasNext = page < totalPages;
//            
//            // 5. 응답 DTO 생성
//            ChangeProductListRespDto responseDto = ChangeProductListRespDto.builder()
//                    .changeItems(changeItemDtos)
//                    .pagination(ChangeProductListRespDto.PaginationDto.builder()
//                            .currentPage(page)
//                            .totalPage(totalPages)
//                            .size(5)
//                            .hasNext(hasNext)
//                            .build())
//                    .build();
//            
//            log.info("=== 교환/반품 상품 목록 조회 완료 - 조회된 항목 수: {}, 총 페이지: {} ===", 
//                    changeItemDtos.size(), totalPages);
//            
//            return RespDto.<ChangeProductListRespDto>builder()
//                    .code(1)
//                    .data(responseDto)
//                    .build();
//                    
//        } catch (Exception e) {
//            log.error("교환/반품 상품 목록 조회 실패 - changeStatus: {}, page: {}, error: {}", 
//                    changeStatus, page, e.getMessage(), e);
//            return RespDto.<ChangeProductListRespDto>builder()
//                    .code(-1)
//                    .data(null)
//                    .build();
//        }
//    }
//    
//    /**
//     * Object[] 배열을 ChangeItemDto로 변환
//     */
//    private ChangeProductListRespDto.ChangeItemDto convertToChangeItemDto(Object[] row) {
//        try {
//            // 이미지 URL 문자열을 List로 변환
//            String imageUrls = (String) row[12];  // GROUP_CONCAT 결과는 마지막 인덱스
//            List<String> productImgUrls = parseImageUrls(imageUrls);
//            
//            // userCode Integer -> Long 변환
//            Long userCode = ((Integer) row[3]).longValue();
//            
//            // Timestamp -> LocalDateTime 변환
//            java.sql.Timestamp timestamp = (java.sql.Timestamp) row[7];
//            java.time.LocalDateTime paymentAt = timestamp.toLocalDateTime();
//            
//            return ChangeProductListRespDto.ChangeItemDto.builder()
//                    .changeId((Integer) row[0])           // ci.change_id
//                    .orderDetailId((Integer) row[1])      // ci.order_detail_id
//                    .orderId((Integer) row[2])            // ci.order_id
//                    .userCode(userCode)                   // ci.user_code (Integer -> Long)
//                    .changeStatus((Integer) row[4])       // ci.change_status
//                    .approvalStatus((Integer) row[5])     // ci.approval_status
//                    .contents((String) row[6])            // ci.contents
//                    .paymentAt(paymentAt)                 // oi.payment_at (Timestamp -> LocalDateTime)
//                    .receivedUserName((String) row[8])    // oi.received_user_name
//                    .price((Integer) row[9])
//                    .productName((String) row[10])        // p.name
//                    .optName((String) row[11])
//                    .productImgUrls(productImgUrls)       // GROUP_CONCAT(pi.img_url)
//                    .build();
//                    
//        } catch (Exception e) {
//            log.error("ChangeItemDto 변환 실패 - row 길이: {}, 오류: {}", row.length, e.getMessage());
//            return null;
//        }
//    }
//    
//    // 이미지 URL 문자열을 List로 변환
//    private List<String> parseImageUrls(String imageUrls) {
//        if (imageUrls == null || imageUrls.trim().isEmpty()) {
//            return List.of();
//        }
//        return List.of(imageUrls.split(","));
//    }
//    
//    // 교환/반품 승인/반려 처리
//    @Transactional
//    public RespDto<Boolean> updateChangeApproval(ChangeApprovalReqDto reqDto) {
//        try {
//            log.info("=== 교환/반품 승인/반려 처리 시작 - changeId: {}, approvalStatus: {} ===", 
//                    reqDto.getChangeId(), reqDto.getApprovalStatus());
//            
//            // 1. 교환/반품 항목 존재 여부 확인
//            Optional<ChangeItem> changeItemOpt = changeItemRepository.findById(reqDto.getChangeId());
//            if (changeItemOpt.isEmpty()) {
//                log.warn("교환/반품 항목을 찾을 수 없음 - changeId: {}", reqDto.getChangeId());
//                return RespDto.<Boolean>builder()
//                        .code(-1)
//                        .data(false)
//                        .build();
//            }
//            
//            ChangeItem changeItem = changeItemOpt.get();
//            Integer changeStatus = changeItem.getChangeStatus();  // 1=교환, 2=반품
//            Integer orderId = changeItem.getOrderId();
//            Integer orderDetailId = changeItem.getOrderDetailId();
//            
//            // 2. approval_status 업데이트
//            int updatedRows = changeItemRepository.updateApprovalStatus(
//                    reqDto.getChangeId(), reqDto.getApprovalStatus());
//            
//            if (updatedRows > 0) {
//                String statusText = getApprovalStatusText(reqDto.getApprovalStatus());
//                log.info("=== 교환/반품 승인/반려 처리 완료 - changeId: {}, 상태: {} ===",
//                        reqDto.getChangeId(), statusText);
//
//                // 3. 반품(changeStatus=2)이고 승인(approvalStatus=1)인 경우 추가 처리
//                if (changeStatus == 2 && reqDto.getApprovalStatus() == 1) {
//                    log.info("=== 반품 승인 추가 처리 시작 - orderId: {}, orderDetailId: {} ===", 
//                            orderId, orderDetailId);
//                    
//                    // 3-1. order_detail의 order_status를 1로 변경
//                    Optional<OrderDetail> orderDetailOpt = orderDetailRepository.findById(orderDetailId);
//                    if (orderDetailOpt.isPresent()) {
//                        OrderDetail orderDetail = orderDetailOpt.get();
//                        orderDetail.setOrderStatus(1);
//                        orderDetailRepository.save(orderDetail);
//                        
//                        log.info("OrderDetail 업데이트 완료 - orderDetailId: {}, orderStatus: 1", orderDetailId);
//                        
//                        // 3-2. 같은 orderId를 가진 모든 OrderDetail 조회
//                        List<OrderDetail> allOrderDetails = orderDetailRepository.findByOrderId(orderId);
//                        
//                        // 3-3. 모든 주문 상세의 orderStatus가 1인지 확인
//                        boolean allCanceled = allOrderDetails.stream()
//                                .allMatch(od -> od.getOrderStatus() != null && od.getOrderStatus() == 1);
//                        
//                        log.info("주문 상세 확인 - orderId: {}, 전체 상품 수: {}, 모두 취소: {}", 
//                                orderId, allOrderDetails.size(), allCanceled);
//                        
//                        // 3-4. 모두 취소되었으면 order_item의 delivery_status를 '주문취소'로 변경
//                        if (allCanceled) {
//                            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderId);
//                            if (orderItemOpt.isPresent()) {
//                                OrderItem orderItem = orderItemOpt.get();
//                                orderItem.setDeliveryStatus("주문취소");
//                                orderItemRepository.save(orderItem);
//                                
//                                log.info("OrderItem 업데이트 완료 - orderId: {}, deliveryStatus: 주문취소", orderId);
//                            } else {
//                                log.warn("OrderItem을 찾을 수 없음 - orderId: {}", orderId);
//                            }
//                        } else {
//                            log.info("일부 상품만 취소됨 - OrderItem 상태 유지");
//                        }
//                    } else {
//                        log.warn("OrderDetail을 찾을 수 없음 - orderDetailId: {}", orderDetailId);
//                    }
//                    
//                    log.info("=== 반품 승인 추가 처리 완료 ===");
//                }
//
//                return RespDto.<Boolean>builder()
//                        .code(1)
//                        .data(true)
//                        .build();
//            } else {
//                log.warn("교환/반품 승인/반려 처리 실패 - 업데이트된 행 없음, changeId: {}", reqDto.getChangeId());
//                return RespDto.<Boolean>builder()
//                        .code(-1)
//                        .data(false)
//                        .build();
//            }
//                    
//        } catch (Exception e) {
//            log.error("교환/반품 승인/반려 처리 실패 - changeId: {}, approvalStatus: {}, error: {}", 
//                    reqDto.getChangeId(), reqDto.getApprovalStatus(), e.getMessage(), e);
//            return RespDto.<Boolean>builder()
//                    .code(-1)
//                    .data(false)
//                    .build();
//        }
//    }
//
//    private String getApprovalStatusText(Integer approvalStatus) {
//        switch (approvalStatus) {
//            case 0: return "승인대기";
//            case 1: return "승인"; 
//            case 2: return "반려";
//            default: return "알 수 없음";
//        }
//    }
    
}