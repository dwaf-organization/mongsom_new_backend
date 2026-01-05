package com.mongsom.dev.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import com.mongsom.dev.dto.change.reqDto.ChangeCreateReqDto;
import com.mongsom.dev.dto.change.reqDto.ChangeDeleteReqDto;
import com.mongsom.dev.dto.delivery.respDto.DeliveryCountRespDto;
import com.mongsom.dev.dto.delivery.respDto.DeliveryInfoRespDto;
import com.mongsom.dev.dto.order.respDto.MyOrderDetailRespDto;
import com.mongsom.dev.dto.order.respDto.MyOrderListRespDto;
import com.mongsom.dev.dto.review.reqDto.ReviewCreateReqDto;
import com.mongsom.dev.dto.review.reqDto.ReviewUpdateReqDto;
import com.mongsom.dev.dto.review.respDto.AdminReviewDetailRespDto;
import com.mongsom.dev.dto.review.respDto.AdminReviewListRespDto;
import com.mongsom.dev.dto.review.respDto.MyReviewRespDto;
import com.mongsom.dev.service.ChangeService;
import com.mongsom.dev.service.MyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/my")
@RequiredArgsConstructor
@Slf4j
public class MyController {
    
    private final MyService myService;
    private final ChangeService changeService;
    
    /**
     * 작성 가능한 리뷰 조회 (review_status = 0)
     */
    @GetMapping("/review/{userCode}/{page}/{size}")
    public ResponseEntity<RespDto<MyReviewRespDto>> getWritableReviews(
            @PathVariable("userCode") Long userCode,
            @PathVariable("page") Integer page,
            @PathVariable("size") Integer size) {
        
        log.info("=== 작성 가능한 리뷰 조회 요청 ===");
        log.info("userCode: {}, page: {}, size: {}", userCode, page, size);
        
        // 파라미터 유효성 검증
        if (userCode == null || userCode <= 0) {
            return ResponseEntity.badRequest().body(
                RespDto.<MyReviewRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        if (page < 0) {
            return ResponseEntity.badRequest().body(
                RespDto.<MyReviewRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        if (size < 1 || size > 100) {
            return ResponseEntity.badRequest().body(
                RespDto.<MyReviewRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        Pageable pageable = PageRequest.of(page, size);
        RespDto<MyReviewRespDto> response = myService.getWritableReviews(userCode, pageable);
        
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        
        log.info("작성 가능한 리뷰 조회 완료 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 작성된 리뷰 조회 (review_status = 1)
     */
    @GetMapping("/review/write/{userCode}/{page}/{size}")
    public ResponseEntity<RespDto<MyReviewRespDto>> getWrittenReviews(
            @PathVariable("userCode") Long userCode,
            @PathVariable("page") Integer page,
            @PathVariable("size") Integer size) {
        
        log.info("=== 작성된 리뷰 조회 요청 ===");
        log.info("userCode: {}, page: {}, size: {}", userCode, page, size);
        
        // 파라미터 유효성 검증 (동일)
        if (userCode == null || userCode <= 0) {
            return ResponseEntity.badRequest().body(
                RespDto.<MyReviewRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        if (page < 0) {
            return ResponseEntity.badRequest().body(
                RespDto.<MyReviewRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        if (size < 1 || size > 100) {
            return ResponseEntity.badRequest().body(
                RespDto.<MyReviewRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        Pageable pageable = PageRequest.of(page, size);
        RespDto<MyReviewRespDto> response = myService.getWrittenReviews(userCode, pageable);
        
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        
        log.info("작성된 리뷰 조회 완료 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 리뷰 숨김 처리 (관리자)
     */
    @PutMapping("/review/hide/{reviewId}")
    public ResponseEntity<RespDto<String>> hideReview(
            @PathVariable("reviewId") Integer reviewId) {
        
        log.info("=== 리뷰 숨김 처리 요청 ===");
        log.info("reviewId: {}", reviewId);
        
        if (reviewId == null || reviewId <= 0) {
            log.warn("잘못된 리뷰 ID: {}", reviewId);
            return ResponseEntity.badRequest().body(
                RespDto.<String>builder()
                    .code(-1)
                    .data("유효하지 않은 리뷰 ID입니다.")
                    .build()
            );
        }
        
        RespDto<String> response = myService.hideReview(reviewId);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.NOT_FOUND :
                           HttpStatus.BAD_REQUEST;
        
        log.info("리뷰 숨김 처리 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 리뷰 숨김 해제 (관리자)
     */
    @PutMapping("/review/show/{reviewId}")
    public ResponseEntity<RespDto<String>> showReview(
            @PathVariable("reviewId") Integer reviewId) {
        
        log.info("=== 리뷰 숨김 해제 요청 ===");
        log.info("reviewId: {}", reviewId);
        
        if (reviewId == null || reviewId <= 0) {
            log.warn("잘못된 리뷰 ID: {}", reviewId);
            return ResponseEntity.badRequest().body(
                RespDto.<String>builder()
                    .code(-1)
                    .data("유효하지 않은 리뷰 ID입니다.")
                    .build()
            );
        }
        
        RespDto<String> response = myService.showReview(reviewId);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.NOT_FOUND :
                           HttpStatus.BAD_REQUEST;
        
        log.info("리뷰 숨김 해제 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 리뷰 완전 삭제 (관리자, 하드 딜리트)
     */
    @DeleteMapping("/review/delete/{reviewId}")
    public ResponseEntity<RespDto<String>> deleteReview(
            @PathVariable("reviewId") Integer reviewId) {
        
        log.info("=== 리뷰 완전 삭제 요청 ===");
        log.info("reviewId: {}", reviewId);
        
        if (reviewId == null || reviewId <= 0) {
            log.warn("잘못된 리뷰 ID: {}", reviewId);
            return ResponseEntity.badRequest().body(
                RespDto.<String>builder()
                    .code(-1)
                    .data("유효하지 않은 리뷰 ID입니다.")
                    .build()
            );
        }
        
        RespDto<String> response = myService.deleteReview(reviewId);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.NOT_FOUND :
                           HttpStatus.INTERNAL_SERVER_ERROR;
        
        log.info("리뷰 완전 삭제 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 전체 리뷰 목록 조회 (관리자, 숨김 포함)
     */
    @GetMapping("/all")
    public ResponseEntity<RespDto<AdminReviewListRespDto>> getAllReviews(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "adminHidden", required = false) Integer adminHidden) {
        
        log.info("=== 전체 리뷰 목록 조회 (관리자) ===");
        log.info("page: {}, size: {}, adminHidden: {}", page, size, adminHidden);
        
        // 파라미터 유효성 검증
        if (page < 0) {
            return ResponseEntity.badRequest().body(
                RespDto.<AdminReviewListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        if (size < 1 || size > 100) {
            return ResponseEntity.badRequest().body(
                RespDto.<AdminReviewListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        Pageable pageable = PageRequest.of(page, size);
        RespDto<AdminReviewListRespDto> response = myService.getAllReviewsForAdmin(pageable, adminHidden);
        
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        
        log.info("전체 리뷰 목록 조회 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 리뷰 상세 조회 (관리자용)
     */
    @GetMapping("/detail/{reviewId}")
    public ResponseEntity<RespDto<AdminReviewDetailRespDto>> getReviewDetail(
            @PathVariable("reviewId") Integer reviewId) {
        
        log.info("=== 리뷰 상세 조회 (관리자) ===");
        log.info("reviewId: {}", reviewId);
        
        if (reviewId == null || reviewId <= 0) {
            return ResponseEntity.badRequest().body(
                RespDto.<AdminReviewDetailRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        RespDto<AdminReviewDetailRespDto> response = myService.getReviewDetailForAdmin(reviewId);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.NOT_FOUND :
                           HttpStatus.INTERNAL_SERVER_ERROR;
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 리뷰 작성
     */
    @PostMapping("/review/create")
    public ResponseEntity<RespDto<String>> createReview(
            @Valid @RequestBody ReviewCreateReqDto reqDto) {
        
        log.info("=== 리뷰 작성 요청 ===");
        log.info("userCode: {}, orderDetailId: {}, productId: {}", 
                reqDto.getUserCode(), reqDto.getOrderDetailId(), reqDto.getProductId());
        
        RespDto<String> response = myService.createReview(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.NOT_FOUND :
                           response.getCode() == -3 ? HttpStatus.CONFLICT :
                           HttpStatus.BAD_REQUEST;
        
        log.info("리뷰 작성 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 리뷰 수정
     */
    @PutMapping("/review/update")
    public ResponseEntity<RespDto<String>> updateReview(
            @Valid @RequestBody ReviewUpdateReqDto reqDto) {
        
        log.info("=== 리뷰 수정 요청 ===");
        log.info("userCode: {}, reviewId: {}", reqDto.getUserCode(), reqDto.getReviewId());
        
        RespDto<String> response = myService.updateReview(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.NOT_FOUND :
                           response.getCode() == -3 ? HttpStatus.FORBIDDEN :
                           HttpStatus.BAD_REQUEST;
        
        log.info("리뷰 수정 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 최근 3개월 내 배송 건수 조회
     */
    @GetMapping("/delivery/number/{userCode}")
    public ResponseEntity<RespDto<DeliveryCountRespDto>> getDeliveryCount(
            @PathVariable("userCode") Long userCode) {
        
        log.info("=== 배송 건수 조회 요청 ===");
        log.info("userCode: {}", userCode);
        
        RespDto<DeliveryCountRespDto> response = myService.getDeliveryCount(userCode);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("배송 건수 조회 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 주문내역 조회 (페이징)
     */
    @GetMapping("/order/{userCode}")
    public ResponseEntity<RespDto<MyOrderListRespDto>> getMyOrderList(
            @PathVariable("userCode") Long userCode,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        
        log.info("=== 주문내역 조회 요청 ===");
        log.info("userCode: {}, page: {}, size: {}", userCode, page, size);
        
        // 페이징 유효성 검증
        if (page < 0) {
            log.warn("잘못된 page 값 - page: {}", page);
            return ResponseEntity.badRequest().body(
                    RespDto.<MyOrderListRespDto>builder()
                            .code(-1)
                            .data(null)
                            .build()
            );
        }
        
        if (size < 1 || size > 100) {
            log.warn("잘못된 size 값 - size: {}", size);
            return ResponseEntity.badRequest().body(
                    RespDto.<MyOrderListRespDto>builder()
                            .code(-1)
                            .data(null)
                            .build()
            );
        }
        
        Pageable pageable = PageRequest.of(page, size);
        RespDto<MyOrderListRespDto> response = myService.getMyOrderList(userCode, pageable);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("주문내역 조회 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 주문상세 조회
     */
    @GetMapping("/order/detail/{orderId}")
    public ResponseEntity<RespDto<MyOrderDetailRespDto>> getMyOrderDetail(
            @PathVariable("orderId") Integer orderId) {
        
        log.info("=== 주문상세 조회 요청 ===");
        log.info("orderId: {}", orderId);
        
        RespDto<MyOrderDetailRespDto> response = myService.getMyOrderDetail(orderId);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("주문상세 조회 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 주문별 배송정보 조회
     */
    @GetMapping("/delivery/find/{orderId}")
    public ResponseEntity<RespDto<DeliveryInfoRespDto>> getDeliveryInfo(
            @PathVariable("orderId") Integer orderId) {
        
        log.info("=== 배송정보 조회 요청 ===");
        log.info("orderId: {}", orderId);
        
        RespDto<DeliveryInfoRespDto> response = myService.getDeliveryInfo(orderId);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("배송정보 조회 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    /**
     * 교환/반품 신청
     */
    @PostMapping("/change/create")
    public ResponseEntity<RespDto<String>> createChangeRequest(
            @Valid @RequestBody ChangeCreateReqDto reqDto) {
        
        log.info("=== 교환/반품 신청 요청 ===");
        log.info("userCode: {}, orderDetailId: {}, changeType: {}", 
                reqDto.getUserCode(), reqDto.getOrderDetailId(), reqDto.getChangeType());
        
        RespDto<String> response = changeService.createChangeRequest(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.FORBIDDEN :
                           response.getCode() == -3 ? HttpStatus.BAD_REQUEST :
                           response.getCode() == -4 ? HttpStatus.CONFLICT :
                           HttpStatus.BAD_REQUEST;
        
        log.info("교환/반품 신청 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 교환/반품 신청 취소
     */
    @DeleteMapping("/change/delete")
    public ResponseEntity<RespDto<String>> deleteChangeRequest(
            @Valid @RequestBody ChangeDeleteReqDto reqDto) {
        
        log.info("=== 교환/반품 취소 요청 ===");
        log.info("userCode: {}, orderDetailId: {}", reqDto.getUserCode(), reqDto.getOrderDetailId());
        
        RespDto<String> response = changeService.deleteChangeRequest(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.CONFLICT :
                           HttpStatus.BAD_REQUEST;
        
        log.info("교환/반품 취소 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
}