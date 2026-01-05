package com.mongsom.dev.service;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.review.reqDto.ReviewCreateReqDto;
import com.mongsom.dev.dto.review.reqDto.ReviewUpdateReqDto;
import com.mongsom.dev.dto.review.respDto.AdminReviewDetailRespDto;
import com.mongsom.dev.dto.review.respDto.AdminReviewListRespDto;
import com.mongsom.dev.dto.review.respDto.MyReviewRespDto;
import com.mongsom.dev.dto.review.respDto.WrittenReviewRespDto;
import com.mongsom.dev.entity.ChangeItem;
import com.mongsom.dev.entity.OrderDetail;
import com.mongsom.dev.entity.OrderItem;
import com.mongsom.dev.entity.Payments;
import com.mongsom.dev.entity.Product;
import com.mongsom.dev.entity.ReviewImg;
import com.mongsom.dev.entity.User;
import com.mongsom.dev.entity.UserReview;
import com.mongsom.dev.repository.ChangeItemRepository;
import com.mongsom.dev.repository.OrderDetailRepository;
import com.mongsom.dev.repository.OrderItemRepository;
import com.mongsom.dev.repository.PaymentsRepository;
import com.mongsom.dev.repository.ProductImgRepository;
import com.mongsom.dev.repository.ProductRepository;
import com.mongsom.dev.repository.ReviewImgRepository;
import com.mongsom.dev.repository.UserRepository;
import com.mongsom.dev.repository.UserReviewRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyService {
    
    private final OrderItemRepository orderItemRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductImgRepository productImgRepository;
    private final ProductRepository productRepository;
    private final PaymentsRepository paymentsRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final UserRepository userRepository;
    private final UserReviewRepository userReviewRepository;
    private final ChangeItemRepository changeItemRepository;
    
    /**
     * 작성 가능한 리뷰 조회 (review_status = 0, 배송완료)
     */
    @Transactional
    public RespDto<MyReviewRespDto> getWritableReviews(Long userCode, Pageable pageable) {
        try {
            log.info("작성 가능한 리뷰 조회 시작 - userCode: {}", userCode);
            
            // 사용자 존재 확인
            if (!userRepository.existsByUserCode(userCode)) {
                log.warn("존재하지 않는 사용자 - userCode: {}", userCode);
                return RespDto.<MyReviewRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            // 작성 가능한 리뷰 조회 (review_status = 0, 배송완료)
            Page<OrderDetail> orderDetailPage = orderDetailRepository
                    .findReviewableOrderDetails(userCode, pageable);
            
            // DTO 변환
            List<MyReviewRespDto.MyReviewItemDto> items = orderDetailPage.getContent()
                    .stream()
                    .map(this::convertToMyReviewItemDto)
                    .collect(Collectors.toList());
            
            // 페이지 정보 생성
            MyReviewRespDto.PaginationDto pagination = MyReviewRespDto.PaginationDto.builder()
                    .currentPage(orderDetailPage.getNumber())
                    .totalPage(orderDetailPage.getTotalPages())
                    .size(orderDetailPage.getSize())
                    .totalElements(orderDetailPage.getTotalElements())
                    .hasNext(orderDetailPage.hasNext())
                    .hasPrevious(orderDetailPage.hasPrevious())
                    .build();
            
            // 응답 데이터 생성
            MyReviewRespDto responseData = MyReviewRespDto.builder()
                    .items(items)
                    .pagination(pagination)
                    .build();
            
            log.info("작성 가능한 리뷰 조회 완료 - userCode: {}, 총 {}건", 
                    userCode, orderDetailPage.getTotalElements());
            
            return RespDto.<MyReviewRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("작성 가능한 리뷰 조회 실패 - userCode: {}", userCode, e);
            return RespDto.<MyReviewRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * 작성된 리뷰 조회 (review_status = 1)
     */
    @Transactional
    public RespDto<MyReviewRespDto> getWrittenReviews(Long userCode, Pageable pageable) {
        try {
            log.info("작성된 리뷰 조회 시작 - userCode: {}", userCode);
            
            // 사용자 존재 확인
            if (!userRepository.existsByUserCode(userCode)) {
                log.warn("존재하지 않는 사용자 - userCode: {}", userCode);
                return RespDto.<MyReviewRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            // 작성된 리뷰 조회 (review_status = 1)
            Page<OrderDetail> orderDetailPage = orderDetailRepository
                    .findWrittenReviews(userCode, pageable);
            
            // DTO 변환
            List<MyReviewRespDto.MyReviewItemDto> items = orderDetailPage.getContent()
                    .stream()
                    .map(this::convertToMyReviewItemDto)
                    .collect(Collectors.toList());
            
            // 페이지 정보 생성
            MyReviewRespDto.PaginationDto pagination = MyReviewRespDto.PaginationDto.builder()
                    .currentPage(orderDetailPage.getNumber())
                    .totalPage(orderDetailPage.getTotalPages())
                    .size(orderDetailPage.getSize())
                    .totalElements(orderDetailPage.getTotalElements())
                    .hasNext(orderDetailPage.hasNext())
                    .hasPrevious(orderDetailPage.hasPrevious())
                    .build();
            
            // 응답 데이터 생성
            MyReviewRespDto responseData = MyReviewRespDto.builder()
                    .items(items)
                    .pagination(pagination)
                    .build();
            
            log.info("작성된 리뷰 조회 완료 - userCode: {}, 총 {}건", 
                    userCode, orderDetailPage.getTotalElements());
            
            return RespDto.<MyReviewRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("작성된 리뷰 조회 실패 - userCode: {}", userCode, e);
            return RespDto.<MyReviewRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * OrderDetail을 MyReviewItemDto로 변환 (가격 정보 제거)
     */
    private MyReviewRespDto.MyReviewItemDto convertToMyReviewItemDto(OrderDetail orderDetail) {
        // 상품 정보 가져오기
        var product = orderDetail.getProduct();
        
        // 상품 이미지들 수집
        List<String> productImgUrls = new ArrayList<>();
        if (product != null && product.getProductImages() != null) {
            productImgUrls = product.getProductImages()
                    .stream()
                    .map(img -> img.getProductImgUrl())
                    .collect(Collectors.toList());
        }
        
        // 선택된 옵션들 정보 수집
        List<MyReviewRespDto.OptionInfoDto> selectedOptions = new ArrayList<>();
        
        if (orderDetail.hasOption1()) {
            var option1Info = getOptionInfo(orderDetail.getOption1());
            if (option1Info != null) {
                selectedOptions.add(option1Info);
            }
        }
        
        if (orderDetail.hasOption2()) {
            var option2Info = getOptionInfo(orderDetail.getOption2());
            if (option2Info != null) {
                selectedOptions.add(option2Info);
            }
        }
        
        // OrderItem 정보 가져오기
        var orderItem = orderDetail.getOrderItem();
        
        return MyReviewRespDto.MyReviewItemDto.builder()
                // 기본 정보
                .orderDetailId(orderDetail.getOrderDetailId())
                .productId(orderDetail.getProductId())
                .reviewStatus(orderDetail.getReviewStatus())
                
                // 상품 정보 (가격 제거됨)
                .productName(product != null ? product.getName() : "알 수 없는 상품")
                .productImgUrls(productImgUrls)
                
                // 옵션 정보
                .option1(orderDetail.getOption1())
                .option2(orderDetail.getOption2())
                .selectedOptions(selectedOptions)
                
                // 주문 정보 (OrderItem에서)
                .quantity(orderDetail.getQuantity())
                .orderNum(orderItem != null ? orderItem.getOrderNum() : null)
                .orderCreatedAt(orderItem != null ? orderItem.getCreatedAt() : null)
                .paymentAt(orderItem != null ? orderItem.getPaymentAt() : null)
                .deliveryStatus(orderItem != null ? orderItem.getDeliveryStatus() : null)
                .build();
    }
    
    /**
     * 옵션 정보 조회 헬퍼 메서드
     */
    private MyReviewRespDto.OptionInfoDto getOptionInfo(Integer optionValueId) {
        
        return null; // 실제로는 위 코드로 구현
    }
	
    /**
     * 리뷰 숨김 처리 (관리자)
     */
    @Transactional
    public RespDto<String> hideReview(Integer reviewId) {
        try {
            log.info("리뷰 숨김 처리 시작 - reviewId: {}", reviewId);
            
            Optional<UserReview> reviewOpt = userReviewRepository.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                return RespDto.<String>builder()
                        .code(-2)
                        .data("존재하지 않는 리뷰입니다.")
                        .build();
            }
            
            UserReview review = reviewOpt.get();
            review.hideByAdmin();
            userReviewRepository.save(review);
            
            log.info("리뷰 숨김 처리 완료 - reviewId: {}", reviewId);
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("리뷰가 숨김 처리되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("리뷰 숨김 처리 실패 - reviewId: {}", reviewId, e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("리뷰 숨김 처리 중 오류가 발생했습니다.")
                    .build();
        }
    }

    /**
     * 리뷰 숨김 해제 (관리자)
     */
    @Transactional
    public RespDto<String> showReview(Integer reviewId) {
        try {
            log.info("리뷰 숨김 해제 시작 - reviewId: {}", reviewId);
            
            Optional<UserReview> reviewOpt = userReviewRepository.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                log.warn("존재하지 않는 리뷰 - reviewId: {}", reviewId);
                return RespDto.<String>builder()
                        .code(-2)
                        .data("존재하지 않는 리뷰입니다.")
                        .build();
            }
            
            UserReview review = reviewOpt.get();
            review.showByAdmin();
            userReviewRepository.save(review);
            
            log.info("리뷰 숨김 해제 완료 - reviewId: {}", reviewId);
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("리뷰 숨김이 해제되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("리뷰 숨김 해제 실패 - reviewId: {}", reviewId, e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("리뷰 숨김 해제 중 오류가 발생했습니다.")
                    .build();
        }
    }
    
    /**
     * 리뷰 완전 삭제 (관리자, 하드 딜리트)
     */
    @Transactional
    public RespDto<String> deleteReview(Integer reviewId) {
        try {
            log.info("리뷰 완전 삭제 시작 - reviewId: {}", reviewId);
            
            Optional<UserReview> reviewOpt = userReviewRepository.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                return RespDto.<String>builder()
                        .code(-2)
                        .data("존재하지 않는 리뷰입니다.")
                        .build();
            }
            
            UserReview review = reviewOpt.get();
            Integer orderDetailId = review.getOrderDetailId();
            Integer productId = review.getProductId();
            
            // 1. 연관된 OrderDetail의 review_status를 0으로 변경
            Optional<OrderDetail> orderDetailOpt = orderDetailRepository.findById(orderDetailId);
            if (orderDetailOpt.isPresent()) {
                OrderDetail orderDetail = orderDetailOpt.get();
                orderDetail.setReviewStatus(0);
                orderDetailRepository.save(orderDetail);
                log.info("OrderDetail.reviewStatus를 0으로 변경 - orderDetailId: {}", orderDetailId);
            }
            
            // 2. 리뷰 이미지 파일 경로 수집 (물리적 파일 삭제용)
            List<String> imageUrls = new ArrayList<>();
            if (review.getReviewImages() != null) {
                imageUrls = review.getReviewImages().stream()
                        .map(img -> img.getReviewImgUrl()) // 실제 필드명에 맞게 수정 필요
                        .collect(Collectors.toList());
            }
            
            // 3. 리뷰 삭제 (Cascade로 이미지도 함께 삭제됨)
            userReviewRepository.delete(review);
            
            log.info("리뷰 완전 삭제 완료 - reviewId: {}, orderDetailId: {}", reviewId, orderDetailId);
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("리뷰가 완전히 삭제되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("리뷰 완전 삭제 실패 - reviewId: {}", reviewId, e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("리뷰 삭제 중 오류가 발생했습니다.")
                    .build();
        }
    }
    
    /**
     * 전체 리뷰 목록 조회 (관리자, 숨김 포함)
     */
    @Transactional
    public RespDto<AdminReviewListRespDto> getAllReviewsForAdmin(Pageable pageable, Integer adminHidden) {
        try {
            log.info("전체 리뷰 목록 조회 (관리자) - adminHidden: {}", adminHidden);
            
            Page<UserReview> reviewPage;
            
            if (adminHidden != null) {
                // 특정 숨김 상태만 조회
                reviewPage = userReviewRepository.findByAdminHidden(adminHidden, pageable);
            } else {
                // 전체 조회 (숨김 포함)
                reviewPage = userReviewRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
            
            List<AdminReviewListRespDto.AdminReviewItemDto> reviews = reviewPage.getContent()
                    .stream()
                    .map(this::convertToAdminReviewItemDto)
                    .collect(Collectors.toList());
            
            AdminReviewListRespDto.PaginationDto pagination = AdminReviewListRespDto.PaginationDto.builder()
                    .currentPage(reviewPage.getNumber())
                    .totalPage(reviewPage.getTotalPages())
                    .size(reviewPage.getSize())
                    .totalElements(reviewPage.getTotalElements())
                    .hasNext(reviewPage.hasNext())
                    .hasPrevious(reviewPage.hasPrevious())
                    .build();
            
            AdminReviewListRespDto responseData = AdminReviewListRespDto.builder()
                    .reviews(reviews)
                    .pagination(pagination)
                    .build();
            
            return RespDto.<AdminReviewListRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("전체 리뷰 목록 조회 실패", e);
            return RespDto.<AdminReviewListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }

    /**
     * UserReview를 AdminReviewItemDto로 변환 (수정됨)
     */
    private AdminReviewListRespDto.AdminReviewItemDto convertToAdminReviewItemDto(UserReview review) {
        // 1. 사용자명 조회
        String userName = "알 수 없음";
        Optional<User> userOpt = userRepository.findByUserCode(review.getUserCode());
        if (userOpt.isPresent()) {
            userName = userOpt.get().getName(); // 실제 User 엔티티의 이름 필드명에 맞게 수정
        }
        
        // 2. 상품명 조회
        String productName = "알 수 없음";
        Optional<Product> productOpt = productRepository.findById(review.getProductId());
        if (productOpt.isPresent()) {
            productName = productOpt.get().getName(); // 실제 Product 엔티티의 이름 필드명에 맞게 수정
        }
        
        // 3. 주문번호 조회
        String orderNum = null;
        Optional<OrderItem> orderItemOpt = orderItemRepository.findById(review.getOrderId());
        if (orderItemOpt.isPresent()) {
            orderNum = orderItemOpt.get().getOrderNum();
        }
        
        // 4. 리뷰 내용 요약 (50자 제한)
        String contentSummary = review.getReviewContent();
        if (contentSummary != null && contentSummary.length() > 50) {
            contentSummary = contentSummary.substring(0, 50) + "...";
        }
        
        return AdminReviewListRespDto.AdminReviewItemDto.builder()
                .reviewId(review.getReviewId())
                .userCode(review.getUserCode())
                .userName(userName)
                .orderDetailId(review.getOrderDetailId())
                .productId(review.getProductId())
                .productName(productName)
                .orderId(review.getOrderId())
                .orderNum(orderNum)
                .reviewRating(review.getReviewRating())
                .reviewContent(contentSummary)
                .adminHidden(review.getAdminHidden())
                .hiddenStatus(review.isHidden() ? "숨김" : "정상")
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
	
    /**
     * 리뷰 상세 조회 (관리자용)
     */
    @Transactional
    public RespDto<AdminReviewDetailRespDto> getReviewDetailForAdmin(Integer reviewId) {
        try {
            log.info("리뷰 상세 조회 (관리자) 시작 - reviewId: {}", reviewId);
            
            Optional<UserReview> reviewOpt = userReviewRepository.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                log.warn("존재하지 않는 리뷰 - reviewId: {}", reviewId);
                return RespDto.<AdminReviewDetailRespDto>builder()
                        .code(-2)
                        .data(null)
                        .build();
            }
            
            UserReview review = reviewOpt.get();
            AdminReviewDetailRespDto responseData = convertToAdminReviewDetailDto(review);
            
            log.info("리뷰 상세 조회 완료 - reviewId: {}", reviewId);
            
            return RespDto.<AdminReviewDetailRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("리뷰 상세 조회 실패 - reviewId: {}", reviewId, e);
            return RespDto.<AdminReviewDetailRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * UserReview를 AdminReviewDetailDto로 변환 (수정됨)
     */
    private AdminReviewDetailRespDto convertToAdminReviewDetailDto(UserReview review) {
        // 1. 사용자명 조회
        String userName = "알 수 없음";
        Optional<User> userOpt = userRepository.findByUserCode(review.getUserCode());
        if (userOpt.isPresent()) {
            userName = userOpt.get().getName(); // 실제 User 엔티티의 필드명에 맞게 수정
        }
        
        // 2. 상품명 조회
        String productName = "알 수 없음";
        Optional<Product> productOpt = productRepository.findById(review.getProductId());
        if (productOpt.isPresent()) {
            productName = productOpt.get().getName(); // 실제 Product 엔티티의 필드명에 맞게 수정
        }
        
        // 3. 주문 정보 조회
        String orderNum = null;
        AdminReviewDetailRespDto.OrderDetailInfo orderDetailInfo = null;
        
        // OrderItem 조회
        Optional<OrderItem> orderItemOpt = orderItemRepository.findById(review.getOrderId());
        OrderItem orderItem = orderItemOpt.orElse(null);
        
        // OrderDetail 조회
        Optional<OrderDetail> orderDetailOpt = orderDetailRepository.findById(review.getOrderDetailId());
        OrderDetail orderDetail = orderDetailOpt.orElse(null);
        
        if (orderItem != null && orderDetail != null) {
            orderNum = orderItem.getOrderNum();
            
            // 옵션 요약 생성 (간단한 방식)
            String optionSummary = "";
            if (orderDetail.hasOption1() || orderDetail.hasOption2()) {
                optionSummary = String.format("option1: %s, option2: %s", 
                    orderDetail.getOption1(), orderDetail.getOption2());
            }
            
            orderDetailInfo = AdminReviewDetailRespDto.OrderDetailInfo.builder()
                    .quantity(orderDetail.getQuantity())
                    .option1(orderDetail.getOption1())
                    .option2(orderDetail.getOption2())
                    .optionSummary(optionSummary)
                    .orderDate(orderItem.getCreatedAt())
                    .paymentDate(orderItem.getPaymentAt())
                    .deliveryStatus(orderItem.getDeliveryStatus())
                    .build();
        }
        
        // 4. 리뷰 이미지들 수집 (있다면)
        List<String> reviewImageUrls = new ArrayList<>();
        // TODO: UserReviewImage 테이블이 있다면 조회
        /*
        if (review.getReviewImages() != null) {
            reviewImageUrls = review.getReviewImages().stream()
                    .map(img -> img.getReviewImgUrl())
                    .collect(Collectors.toList());
        }
        */
        
        return AdminReviewDetailRespDto.builder()
                .reviewId(review.getReviewId())
                .userCode(review.getUserCode())
                .userName(userName)
                .orderDetailId(review.getOrderDetailId())
                .productId(review.getProductId())
                .productName(productName)
                .orderId(review.getOrderId())
                .orderNum(orderNum)
                .reviewRating(review.getReviewRating())
                .reviewContent(review.getReviewContent()) // 전체 내용
                .adminHidden(review.getAdminHidden())
                .hiddenStatus(review.isHidden() ? "숨김" : "정상")
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .reviewImageUrls(reviewImageUrls)
                .orderDetail(orderDetailInfo)
                .build();
    }
    
}