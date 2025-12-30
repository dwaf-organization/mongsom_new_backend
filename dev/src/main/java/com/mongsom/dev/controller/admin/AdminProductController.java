package com.mongsom.dev.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import com.mongsom.dev.service.admin.AdminProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/product")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {
    
    private final AdminProductService adminProductService;
    
    // 새로운 상품 등록 (옵션 시스템 개선)
    @PostMapping("/regist")
    public ResponseEntity<RespDto<AdminProductRegistRespDto>> registProduct(
            @Valid @RequestBody AdminProductRegistReqDto reqDto) {
        
        log.info("=== 상품 등록 요청 ===");
        log.info("상품명: {}", reqDto.getName());
        log.info("기본가격: {}", reqDto.getBasePrice());
        log.info("옵션타입 개수: {}", reqDto.getOptionTypes() != null ? reqDto.getOptionTypes().size() : 0);
        log.info("이미지 개수: {}", reqDto.getProductImgUrls() != null ? reqDto.getProductImgUrls().size() : 0);
        
        // 옵션 상세 로깅
        if (reqDto.getOptionTypes() != null) {
            reqDto.getOptionTypes().forEach(optionType -> {
                log.info("옵션타입: {} (필수: {}, 값개수: {})", 
                        optionType.getTypeName(), 
                        optionType.getIsRequired(),
                        optionType.getOptionValues() != null ? optionType.getOptionValues().size() : 0);
                
                if (optionType.getOptionValues() != null) {
                    optionType.getOptionValues().forEach(optionValue -> {
                        log.info("  - {} (가격조정: {}, 재고상태: {})", 
                                optionValue.getValueName(),
                                optionValue.getPriceAdjustment(),
                                optionValue.getStockStatus());
                    });
                }
            });
        }
        
        RespDto<AdminProductRegistRespDto> response = adminProductService.registProduct(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("상품 등록 결과 - code: {}", response.getCode());
        if (response.getData() != null) {
            log.info("등록된 상품 ID: {}", response.getData().getProductId());
        }
        
        return ResponseEntity.status(status).body(response);
    }

    // 상품 목록 조회 (검색 조건 포함)
    @GetMapping("/select/{page}")
    public ResponseEntity<RespDto<AdminProductListRespDto>> getProductList(
            @PathVariable("page") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "premium", defaultValue = "2") Integer premium,
            @RequestParam(value = "outOfStock", defaultValue = "0") Integer outOfStock,
            @RequestParam(value = "paused", defaultValue = "0") Integer paused) {
        
        log.info("=== 관리자 상품 목록 조회 요청 ===");
        log.info("페이지: {} (크기: {})", page, size);
        log.info("검색조건:");
        log.info("  - 상품명: {}", name != null ? "'" + name + "'" : "전체");
        log.info("  - 프리미엄: {}", premium == 2 ? "전체" : (premium == 1 ? "프리미엄만" : "일반만"));
        log.info("  - 품절상품만: {}", outOfStock == 1 ? "예" : "아니요");
        log.info("  - 일시정지상품만: {}", paused == 1 ? "예" : "아니요");
        
        // 페이지 번호 검증 (1-based를 0-based로 변환)
        if (page < 1) {
            page = 1;
        }
        
        RespDto<AdminProductListRespDto> response = adminProductService.getProductList(
                page - 1, size, name, premium, outOfStock, paused);
        
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("상품 목록 조회 결과 - code: {}", response.getCode());
        if (response.getData() != null) {
            log.info("조회된 상품 수: {} / 전체: {} ({}페이지 / 총 {}페이지)", 
                    response.getData().getProducts().size(),
                    response.getData().getTotalElements(),
                    response.getData().getCurrentPage() + 1,
                    response.getData().getTotalPages());
        }
        
        return ResponseEntity.status(status).body(response);
    }
    
    // 상품 상세 조회 (수정용)
    @GetMapping("/select/detail/{productId}")
    public ResponseEntity<RespDto<AdminProductDetailRespDto>> getProductDetail(
            @PathVariable("productId") Integer productId) {
        
        log.info("=== 관리자 상품 상세 조회 요청 ===");
        log.info("상품 ID: {}", productId);
        
        RespDto<AdminProductDetailRespDto> response = adminProductService.getProductDetail(productId);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("상품 상세 조회 결과 - code: {}", response.getCode());
        if (response.getData() != null) {
            log.info("상품명: {}", response.getData().getName());
            log.info("옵션 타입 개수: {}", response.getData().getOptionTypes() != null ? 
                     response.getData().getOptionTypes().size() : 0);
            log.info("이미지 개수: {}", response.getData().getProductImages() != null ? 
                     response.getData().getProductImages().size() : 0);
        }
        
        return ResponseEntity.status(status).body(response);
    }

    // 상품 수정
    @PutMapping("/update/{productId}")
    public ResponseEntity<RespDto<AdminProductUpdateRespDto>> updateProduct(
            @PathVariable("productId") Integer productId,
            @Valid @RequestBody AdminProductUpdateReqDto reqDto) {
        
        log.info("=== 관리자 상품 수정 요청 ===");
        log.info("상품 ID: {}", productId);
        log.info("상품명: {}", reqDto.getName());
        log.info("이미지 개수: {}", reqDto.getProductImages() != null ? reqDto.getProductImages().size() : 0);
        log.info("옵션타입 개수: {}", reqDto.getOptionTypes() != null ? reqDto.getOptionTypes().size() : 0);
        
        // 수정 상세 로깅
        if (reqDto.getProductImages() != null) {
            long newImages = reqDto.getProductImages().stream().filter(img -> img.getProductImgId() == null).count();
            long deletedImages = reqDto.getProductImages().stream().filter(img -> img.getIsDeleted()).count();
            log.info("이미지 - 신규: {}, 삭제: {}", newImages, deletedImages);
        }
        
        if (reqDto.getOptionTypes() != null) {
            long newOptionTypes = reqDto.getOptionTypes().stream().filter(opt -> opt.getOptionTypeId() == null).count();
            long deletedOptionTypes = reqDto.getOptionTypes().stream().filter(opt -> opt.getIsDeleted()).count();
            log.info("옵션타입 - 신규: {}, 삭제: {}", newOptionTypes, deletedOptionTypes);
        }
        
        RespDto<AdminProductUpdateRespDto> response = adminProductService.updateProduct(productId, reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("상품 수정 결과 - code: {}", response.getCode());
        if (response.getData() != null && response.getData().getUpdateSummary() != null) {
            AdminProductUpdateRespDto.UpdateSummary summary = response.getData().getUpdateSummary();
            log.info("수정 요약 - 이미지(추가:{}, 수정:{}, 삭제:{}), 옵션타입(추가:{}, 수정:{}, 삭제:{}), 옵션값(추가:{}, 수정:{}, 삭제:{})",
                    summary.getAddedImages(), summary.getUpdatedImages(), summary.getDeletedImages(),
                    summary.getAddedOptionTypes(), summary.getUpdatedOptionTypes(), summary.getDeletedOptionTypes(),
                    summary.getAddedOptionValues(), summary.getUpdatedOptionValues(), summary.getDeletedOptionValues());
        }
        
        return ResponseEntity.status(status).body(response);
    }
    
    // 상품 소프트 삭제
    @PutMapping("/delete/{productId}")
    public ResponseEntity<RespDto<Boolean>> deleteProduct(@PathVariable("productId") Integer productId) {
        
        log.info("=== 관리자 상품 삭제 요청 ===");
        log.info("상품 ID: {}", productId);
        
        RespDto<Boolean> response = adminProductService.softDeleteProduct(productId);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("상품 삭제 결과 - code: {}, success: {}", response.getCode(), response.getData());
        
        return ResponseEntity.status(status).body(response);
    }
    
    // 옵션 타입 삭제
    @PutMapping("/option-type/delete/{optionTypeId}")
    public ResponseEntity<RespDto<Boolean>> deleteOptionType(@PathVariable("optionTypeId") Integer optionTypeId) {
        RespDto<Boolean> response = adminProductService.deleteOptionType(optionTypeId);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    // 옵션 값 삭제
    @PutMapping("/option-value/delete/{optionValueId}")
    public ResponseEntity<RespDto<Boolean>> deleteOptionValue(@PathVariable("optionValueId") Integer optionValueId) {
        RespDto<Boolean> response = adminProductService.deleteOptionValue(optionValueId);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
    
//    // 교환/반품 상품 목록 조회
//    @GetMapping("/change/list/{changeStatus}/{page}")
//    public ResponseEntity<RespDto<ChangeProductListRespDto>> getChangeProductList(
//            @PathVariable("changeStatus") Integer changeStatus,
//            @PathVariable("page") Integer page) {
//
//        log.info("교환/반품 상품 목록 조회 요청 - changeStatus: {}, page: {}", changeStatus, page);
//
//        RespDto<ChangeProductListRespDto> response = adminProductService.getChangeProductList(changeStatus, page);
//
//        return ResponseEntity.ok(response);
//    }
//    
//    // 교환/반품 승인/반려 처리
//    @PutMapping("/change/update")
//    public ResponseEntity<RespDto<Boolean>> updateChangeApproval(
//            @RequestBody ChangeApprovalReqDto reqDto) {
//
//        log.info("교환/반품 승인/반려 처리 요청 - changeId: {}, approvalStatus: {}", 
//                reqDto.getChangeId(), reqDto.getApprovalStatus());
//
//        RespDto<Boolean> response = adminProductService.updateChangeApproval(reqDto);
//
//        return ResponseEntity.ok(response);
//    }
}