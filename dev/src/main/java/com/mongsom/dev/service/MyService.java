package com.mongsom.dev.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.delivery.respDto.DeliveryCountRespDto;
import com.mongsom.dev.dto.delivery.respDto.DeliveryInfoRespDto;
import com.mongsom.dev.dto.order.respDto.MyOrderDetailRespDto;
import com.mongsom.dev.dto.order.respDto.MyOrderListRespDto;
import com.mongsom.dev.dto.review.reqDto.ReviewCreateReqDto;
import com.mongsom.dev.dto.review.reqDto.ReviewUpdateReqDto;
import com.mongsom.dev.dto.review.respDto.AdminReviewDetailRespDto;
import com.mongsom.dev.dto.review.respDto.AdminReviewListRespDto;
import com.mongsom.dev.dto.review.respDto.MyReviewRespDto;
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
import com.mongsom.dev.repository.ProductOptionValueRepository;
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
    private final ProductRepository productRepository;
    private final PaymentsRepository paymentsRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final UserRepository userRepository;
    private final UserReviewRepository userReviewRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
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
     * 작성된 리뷰 조회 (review_status = 1, 임의숨김 제외, 리뷰 내용 포함)
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
            
            // 작성된 리뷰 조회 (리뷰 내용 포함)
            Page<Object[]> resultPage = orderDetailRepository
                    .findWrittenReviewsWithReviewInfo(userCode, pageable);
            
            // DTO 변환
            List<MyReviewRespDto.MyReviewItemDto> items = resultPage.getContent()
                    .stream()
                    .map(this::convertToMyReviewItemDtoWithReview)
                    .collect(Collectors.toList());
            
            // 페이지 정보 생성
            MyReviewRespDto.PaginationDto pagination = MyReviewRespDto.PaginationDto.builder()
                    .currentPage(resultPage.getNumber())
                    .totalPage(resultPage.getTotalPages())
                    .size(resultPage.getSize())
                    .totalElements(resultPage.getTotalElements())
                    .hasNext(resultPage.hasNext())
                    .hasPrevious(resultPage.hasPrevious())
                    .build();
            
            // 응답 데이터 생성
            MyReviewRespDto responseData = MyReviewRespDto.builder()
                    .items(items)
                    .pagination(pagination)
                    .build();
            
            log.info("작성된 리뷰 조회 완료 - userCode: {}, 총 {}건", 
                    userCode, resultPage.getTotalElements());
            
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
        
        // 옵션명 조회
        String option1Name = null;
        String option2Name = null;
        
        if (orderDetail.hasOption1()) {
            option1Name = getOptionValueName(orderDetail.getOption1());
        }
        
        if (orderDetail.hasOption2()) {
            option2Name = getOptionValueName(orderDetail.getOption2());
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
                .option1Name(option1Name)
                .option2Name(option2Name)
                .selectedOptions(selectedOptions)
                
                // 주문 정보 (OrderItem에서)
                .quantity(orderDetail.getQuantity())
                .orderNum(orderItem != null ? orderItem.getOrderNum() : null)
                .orderCreatedAt(orderItem != null ? orderItem.getCreatedAt() : null)
                .paymentAt(orderItem != null ? orderItem.getPaymentAt() : null)
                .deliveryStatus(orderItem != null ? orderItem.getDeliveryStatus() : null)
                
                .reviewId(null)
                .reviewRating(null)
                .reviewContent(null)
                .reviewImgUrls(new ArrayList<>())
                .reviewCreatedAt(null)
                
                .build();
    }
    
    /**
     * OrderDetail + UserReview를 MyReviewItemDto로 변환 (리뷰 내용 포함)
     */
    private MyReviewRespDto.MyReviewItemDto convertToMyReviewItemDtoWithReview(Object[] result) {
        OrderDetail orderDetail = (OrderDetail) result[0];
        UserReview userReview = (UserReview) result[1]; // null일 수 있음
        
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
        
        // 옵션명 조회
        String option1Name = null;
        String option2Name = null;
        
        if (orderDetail.hasOption1()) {
            option1Name = getOptionValueName(orderDetail.getOption1());
        }
        
        if (orderDetail.hasOption2()) {
            option2Name = getOptionValueName(orderDetail.getOption2());
        }
        
        // 선택된 옵션들 정보 수집 (기존 로직)
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
        
        // 리뷰 이미지들 조회
        List<String> reviewImgUrls = new ArrayList<>();
        if (userReview != null) {
            reviewImgUrls = reviewImgRepository.findImageUrlsByReviewId(userReview.getReviewId());
        }
        
        // OrderItem 정보 가져오기
        var orderItem = orderDetail.getOrderItem();
        
        return MyReviewRespDto.MyReviewItemDto.builder()
                // ===== 기본 정보 =====
                .orderDetailId(orderDetail.getOrderDetailId())
                .productId(orderDetail.getProductId())
                .reviewStatus(orderDetail.getReviewStatus())
                
                // ===== 상품 정보 =====
                .productName(product != null ? product.getName() : "알 수 없는 상품")
                .productImgUrls(productImgUrls)
                
                // ===== 옵션 정보 =====
                .option1(orderDetail.getOption1())
                .option2(orderDetail.getOption2())
                .option1Name(option1Name)
                .option2Name(option2Name)
                .selectedOptions(selectedOptions)
                
                // ===== 주문 정보 =====
                .quantity(orderDetail.getQuantity())
                .orderNum(orderItem != null ? orderItem.getOrderNum() : null)
                .orderCreatedAt(orderItem != null ? orderItem.getCreatedAt() : null)
                .paymentAt(orderItem != null ? orderItem.getPaymentAt() : null)
                .deliveryStatus(orderItem != null ? orderItem.getDeliveryStatus() : null)
                
                // ===== 리뷰 내용 (새로 추가) =====
                .reviewId(userReview != null ? userReview.getReviewId() : null)
                .reviewRating(userReview != null ? userReview.getReviewRating() : null)
                .reviewContent(userReview != null ? userReview.getReviewContent() : null)
                .reviewImgUrls(reviewImgUrls)
                .reviewCreatedAt(userReview != null ? userReview.getCreatedAt() : null)
                .build();
    }
    
    /**
     * 옵션 값 이름 조회 헬퍼 메서드
     */
    private String getOptionValueName(Integer optionValueId) {
        try {
            if (optionValueId == null) return null;
            
            Optional<String> valueNameOpt = productOptionValueRepository.findValueNameById(optionValueId);
            return valueNameOpt.orElse("옵션-" + optionValueId);
            
        } catch (Exception e) {
            log.warn("옵션 이름 조회 실패 - optionValueId: {}", optionValueId, e);
            return "옵션-" + optionValueId;
        }
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
        
        // 4. 옵션 정보 조회
        Integer option1 = null;
        Integer option2 = null;
        String option1Name = null;
        String option2Name = null;
        String optionSummary = "";
        
        Optional<OrderDetail> orderDetailOpt = orderDetailRepository.findById(review.getOrderDetailId());
        if (orderDetailOpt.isPresent()) {
            OrderDetail orderDetail = orderDetailOpt.get();
            option1 = orderDetail.getOption1();
            option2 = orderDetail.getOption2();
            
            // 옵션명 조회
            if (option1 != null) {
                option1Name = getOptionValueName(option1);
            }
            if (option2 != null) {
                option2Name = getOptionValueName(option2);
            }
            
            // 옵션 요약 생성
            List<String> optionParts = new ArrayList<>();
            if (option1Name != null) optionParts.add(option1Name);
            if (option2Name != null) optionParts.add(option2Name);
            optionSummary = String.join(", ", optionParts);
        }
        
        // 5. 리뷰 내용 요약 (50자 제한)
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
                .option1(option1)
                .option2(option2)
                .option1Name(option1Name)
                .option2Name(option2Name)
                .optionSummary(optionSummary)
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
     * UserReview를 AdminReviewDetailDto로 변환 (최종 버전)
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

            // 옵션명 조회
            String option1Name = null;
            String option2Name = null;
            
            if (orderDetail.hasOption1()) {
                option1Name = getOptionValueName(orderDetail.getOption1());
            }
            if (orderDetail.hasOption2()) {
                option2Name = getOptionValueName(orderDetail.getOption2());
            }

            // 옵션 요약 생성 (실제 옵션명으로)
            String optionSummary = "";
            List<String> optionParts = new ArrayList<>();
            if (option1Name != null) {
                optionParts.add(option1Name);
            }
            if (option2Name != null) {
                optionParts.add(option2Name);
            }
            if (!optionParts.isEmpty()) {
                optionSummary = String.join(", ", optionParts);
            }

            orderDetailInfo = AdminReviewDetailRespDto.OrderDetailInfo.builder()
                    .quantity(orderDetail.getQuantity())
                    .option1(orderDetail.getOption1())
                    .option2(orderDetail.getOption2())
                    .option1Name(option1Name)
                    .option2Name(option2Name)
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
                .reviewContent(review.getReviewContent())
                .adminHidden(review.getAdminHidden())
                .hiddenStatus(review.isHidden() ? "숨김" : "정상")
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .reviewImageUrls(reviewImageUrls)
                .orderDetail(orderDetailInfo)
                .build();
    }
    
    /**
     * 리뷰 작성
     */
    @Transactional
    public RespDto<String> createReview(ReviewCreateReqDto reqDto) {
        try {
            log.info("리뷰 작성 시작 - userCode: {}, orderDetailId: {}", 
                    reqDto.getUserCode(), reqDto.getOrderDetailId());
            
            // 1. OrderDetail 조회 및 검증
            Optional<OrderDetail> orderDetailOpt = orderDetailRepository.findById(reqDto.getOrderDetailId());
            if (orderDetailOpt.isEmpty()) {
                log.warn("존재하지 않는 주문 상세 - orderDetailId: {}", reqDto.getOrderDetailId());
                return RespDto.<String>builder()
                        .code(-2)
                        .data("존재하지 않는 주문입니다.")
                        .build();
            }
            
            OrderDetail orderDetail = orderDetailOpt.get();
            
            // 2. 이미 리뷰가 작성되었는지 확인
            if (orderDetail.getReviewStatus() == 1) {
                log.warn("이미 리뷰가 작성된 주문 - orderDetailId: {}", reqDto.getOrderDetailId());
                return RespDto.<String>builder()
                        .code(-3)
                        .data("이미 리뷰가 작성된 주문입니다.")
                        .build();
            }
            
            // 3. OrderItem에서 orderId 조회
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderDetail.getOrderId());
            if (orderItemOpt.isEmpty()) {
                log.warn("연관된 주문을 찾을 수 없음 - orderId: {}", orderDetail.getOrderId());
                return RespDto.<String>builder()
                        .code(-2)
                        .data("연관된 주문을 찾을 수 없습니다.")
                        .build();
            }
            
            OrderItem orderItem = orderItemOpt.get();
            
            // 4. UserReview 생성
            UserReview userReview = UserReview.builder()
                    .userCode(reqDto.getUserCode())
                    .orderDetailId(reqDto.getOrderDetailId())
                    .productId(reqDto.getProductId())
                    .orderId(orderItem.getOrderId()) // OrderItem의 orderId
                    .reviewRating(reqDto.getReviewRating())
                    .reviewContent(reqDto.getReviewContent())
                    .adminHidden(0)
                    .build();
            
            UserReview savedReview = userReviewRepository.save(userReview);
            
            // 5. 리뷰 이미지 저장
            if (reqDto.getReviewImgUrls() != null && !reqDto.getReviewImgUrls().isEmpty()) {
                List<ReviewImg> reviewImgs = reqDto.getReviewImgUrls().stream()
                        .filter(url -> url != null && !url.trim().isEmpty())
                        .map(url -> ReviewImg.createReviewImg(savedReview.getReviewId(), url))
                        .collect(Collectors.toList());
                
                reviewImgRepository.saveAll(reviewImgs);
                log.info("리뷰 이미지 저장 완료 - reviewId: {}, 이미지 수: {}", 
                        savedReview.getReviewId(), reviewImgs.size());
            }
            
            // 6. OrderDetail의 reviewStatus 업데이트
            orderDetail.setReviewStatus(1);
            orderDetailRepository.save(orderDetail);
            
            log.info("리뷰 작성 완료 - reviewId: {}, userCode: {}", 
                    savedReview.getReviewId(), reqDto.getUserCode());
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("리뷰가 성공적으로 작성되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("리뷰 작성 실패 - userCode: {}, orderDetailId: {}", 
                    reqDto.getUserCode(), reqDto.getOrderDetailId(), e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("리뷰 작성 중 오류가 발생했습니다.")
                    .build();
        }
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    public RespDto<String> updateReview(ReviewUpdateReqDto reqDto) {
        try {
            log.info("리뷰 수정 시작 - userCode: {}, reviewId: {}", 
                    reqDto.getUserCode(), reqDto.getReviewId());
            
            // 1. 리뷰 조회 및 권한 확인
            Optional<UserReview> reviewOpt = userReviewRepository.findByUserCodeAndReviewId(
                    reqDto.getUserCode(), reqDto.getReviewId());
            
            if (reviewOpt.isEmpty()) {
                // 리뷰 존재 여부 확인
                if (userReviewRepository.existsById(reqDto.getReviewId())) {
                    log.warn("권한 없는 리뷰 수정 시도 - userCode: {}, reviewId: {}", 
                            reqDto.getUserCode(), reqDto.getReviewId());
                    return RespDto.<String>builder()
                            .code(-3)
                            .data("수정 권한이 없습니다.")
                            .build();
                } else {
                    log.warn("존재하지 않는 리뷰 - reviewId: {}", reqDto.getReviewId());
                    return RespDto.<String>builder()
                            .code(-2)
                            .data("존재하지 않는 리뷰입니다.")
                            .build();
                }
            }
            
            UserReview review = reviewOpt.get();
            
            // 2. 리뷰 정보 업데이트
            review.setReviewRating(reqDto.getReviewRating());
            review.setReviewContent(reqDto.getReviewContent());
            
            userReviewRepository.save(review);
            
            // 3. 기존 이미지 삭제
            reviewImgRepository.deleteByReviewId(reqDto.getReviewId());
            
            // 4. 새 이미지 저장
            if (reqDto.getReviewImgUrls() != null && !reqDto.getReviewImgUrls().isEmpty()) {
                List<ReviewImg> reviewImgs = reqDto.getReviewImgUrls().stream()
                        .filter(url -> url != null && !url.trim().isEmpty())
                        .map(url -> ReviewImg.createReviewImg(reqDto.getReviewId(), url))
                        .collect(Collectors.toList());
                
                reviewImgRepository.saveAll(reviewImgs);
                log.info("리뷰 이미지 업데이트 완료 - reviewId: {}, 새 이미지 수: {}", 
                        reqDto.getReviewId(), reviewImgs.size());
            }
            
            log.info("리뷰 수정 완료 - reviewId: {}, userCode: {}", 
                    reqDto.getReviewId(), reqDto.getUserCode());
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("리뷰가 성공적으로 수정되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("리뷰 수정 실패 - userCode: {}, reviewId: {}", 
                    reqDto.getUserCode(), reqDto.getReviewId(), e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("리뷰 수정 중 오류가 발생했습니다.")
                    .build();
        }
    }
    
    /**
     * 최근 3개월 내 배송 건수 조회
     */
    @Transactional
    public RespDto<DeliveryCountRespDto> getDeliveryCount(Long userCode) {
        try {
            log.info("배송 건수 조회 시작 - userCode: {}", userCode);
            
            // 사용자 존재 확인
            if (!userRepository.existsByUserCode(userCode)) {
                log.warn("존재하지 않는 사용자 - userCode: {}", userCode);
                return RespDto.<DeliveryCountRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            // 방법 1: 단일 쿼리로 모든 건수 조회
            DeliveryCountRespDto deliveryCount = getDeliveryCountByNativeQuery(userCode);
            
            log.info("배송 건수 조회 완료 - userCode: {}, 결제완료: {}, 상품준비중: {}, 배송중: {}, 배송완료: {}", 
                    userCode, deliveryCount.getPaymentCompleted(), deliveryCount.getPreparing(), 
                    deliveryCount.getShipping(), deliveryCount.getDelivered());
            
            return RespDto.<DeliveryCountRespDto>builder()
                    .code(1)
                    .data(deliveryCount)
                    .build();
            
        } catch (Exception e) {
            log.error("배송 건수 조회 실패 - userCode: {}", userCode, e);
            return RespDto.<DeliveryCountRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * Native Query 사용 (MySQL DATE_SUB 함수 활용)
     */
    private DeliveryCountRespDto getDeliveryCountByNativeQuery(Long userCode) {
    	List<Object[]> result = orderItemRepository.findDeliveryCountByUserCodeAndRecentThreeMonths(userCode);
        
        
        if (!result.isEmpty()) {
            Object[] row = result.get(0);

            Number paymentCompleted = (Number) row[0];
            Number preparing = (Number) row[1];
            Number shipping = (Number) row[2];
            Number delivered = (Number) row[3];

            return DeliveryCountRespDto.of(
                paymentCompleted.intValue(),
                preparing.intValue(),
                shipping.intValue(),
                delivered.intValue()
            );
        }
        
       
        return DeliveryCountRespDto.empty();
    }
    
    /**
     * 주문내역 조회 (페이징)
     */
    @Transactional
    public RespDto<MyOrderListRespDto> getMyOrderList(Long userCode, Pageable pageable) {
        try {
            log.info("주문내역 조회 시작 - userCode: {}, page: {}, size: {}", 
                    userCode, pageable.getPageNumber(), pageable.getPageSize());
            
            // 사용자 존재 확인
            if (!userRepository.existsByUserCode(userCode)) {
                log.warn("존재하지 않는 사용자 - userCode: {}", userCode);
                return RespDto.<MyOrderListRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            // 주문 내역 조회 (최신순)
            Page<OrderItem> orderItemPage = orderItemRepository
                    .findByUserCodeOrderByPaymentAtDesc(userCode, pageable);
            
            // DTO 변환
            List<MyOrderListRespDto.MyOrderItemDto> orders = orderItemPage.getContent()
                    .stream()
                    .map(this::convertToMyOrderItemDto)
                    .collect(Collectors.toList());
            
            // 페이지 정보 생성
            MyOrderListRespDto.PaginationDto pagination = MyOrderListRespDto.PaginationDto.builder()
                    .currentPage(orderItemPage.getNumber())
                    .totalPage(orderItemPage.getTotalPages())
                    .size(orderItemPage.getSize())
                    .totalElements(orderItemPage.getTotalElements())
                    .hasNext(orderItemPage.hasNext())
                    .hasPrevious(orderItemPage.hasPrevious())
                    .build();
            
            // 응답 데이터 생성
            MyOrderListRespDto responseData = MyOrderListRespDto.builder()
                    .orders(orders)
                    .pagination(pagination)
                    .build();
            
            log.info("주문내역 조회 완료 - userCode: {}, 총 {}건", 
                    userCode, orderItemPage.getTotalElements());
            
            return RespDto.<MyOrderListRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("주문내역 조회 실패 - userCode: {}", userCode, e);
            return RespDto.<MyOrderListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * 주문상세 조회
     */
    @Transactional
    public RespDto<MyOrderDetailRespDto> getMyOrderDetail(Integer orderId) {
        try {
            log.info("주문상세 조회 시작 - orderId: {}", orderId);
            
            // 주문 조회
            Optional<OrderItem> orderItemOpt = orderItemRepository.findByOrderId(orderId);
            if (orderItemOpt.isEmpty()) {
                log.warn("존재하지 않는 주문 - orderId: {}", orderId);
                return RespDto.<MyOrderDetailRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            OrderItem orderItem = orderItemOpt.get();
            
            // 주문의 모든 상품 조회
            List<OrderDetail> orderDetails = orderDetailRepository
                    .findByOrderIdOrderByOrderDetailIdAsc(orderId);
            
            if (orderDetails.isEmpty()) {
                log.warn("주문 상품이 없음 - orderId: {}", orderId);
                return RespDto.<MyOrderDetailRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            // DTO 변환
            MyOrderDetailRespDto responseData = convertToMyOrderDetailRespDto(orderItem, orderDetails);
            
            log.info("주문상세 조회 완료 - orderId: {}, 상품 {}개", 
                    orderId, orderDetails.size());
            
            return RespDto.<MyOrderDetailRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("주문상세 조회 실패 - orderId: {}", orderId, e);
            return RespDto.<MyOrderDetailRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * OrderItem을 MyOrderItemDto로 변환 (주문내역용)
     */
    private MyOrderListRespDto.MyOrderItemDto convertToMyOrderItemDto(OrderItem orderItem) {
        // 첫 번째 상품 조회
        List<OrderDetail> orderDetails = orderDetailRepository
                .findByOrderIdOrderByOrderDetailIdAsc(orderItem.getOrderId());
        
        if (orderDetails.isEmpty()) {
            log.warn("주문 상품이 없음 - orderId: {}", orderItem.getOrderId());
            // 빈 DTO 반환
            return MyOrderListRespDto.MyOrderItemDto.builder()
                    .orderId(orderItem.getOrderId())
                    .orderNum(orderItem.getOrderNum())
                    .paymentAt(orderItem.getPaymentAt())
                    .finalPrice(orderItem.getFinalPrice())
                    .deliveryPrice(orderItem.getDeliveryPrice())
                    .deliveryStatus(orderItem.getDeliveryStatus())
                    .build();
        }
        
        OrderDetail firstOrderDetail = orderDetails.get(0);
        Product firstProduct = firstOrderDetail.getProduct();
        
        // 상품명 생성 (외 N개 포함)
        String productName = firstProduct != null ? firstProduct.getName() : "알 수 없는 상품";
        int totalProductCount = orderDetails.size();
        if (totalProductCount > 1) {
            productName += " 외 " + (totalProductCount - 1) + "개";
        }
        
        // 옵션명 조회
        String option1Name = null;
        String option2Name = null;
        
        if (firstOrderDetail.hasOption1()) {
            option1Name = getOptionValueName(firstOrderDetail.getOption1());
        }
        if (firstOrderDetail.hasOption2()) {
            option2Name = getOptionValueName(firstOrderDetail.getOption2());
        }
        
        return MyOrderListRespDto.MyOrderItemDto.builder()
                .orderId(orderItem.getOrderId())
                .orderNum(orderItem.getOrderNum())
                .paymentAt(orderItem.getPaymentAt())
                
                // 첫 번째 상품 정보
                .productId(firstOrderDetail.getProductId())
                .productName(productName)
                .option1(firstOrderDetail.getOption1())
                .option2(firstOrderDetail.getOption2())
                .option1Name(option1Name)
                .option2Name(option2Name)
                .quantity(firstOrderDetail.getQuantity())
                
                // 주문 정보
                .finalPrice(orderItem.getFinalPrice())
                .deliveryPrice(orderItem.getDeliveryPrice())
                .deliveryStatus(orderItem.getDeliveryStatus())
                .build();
    }
    
    /**
     * OrderItem + OrderDetails를 MyOrderDetailRespDto로 변환
     */
    private MyOrderDetailRespDto convertToMyOrderDetailRespDto(OrderItem orderItem, List<OrderDetail> orderDetails) {
        // 주문 정보
        MyOrderDetailRespDto.OrderInfo orderInfo = MyOrderDetailRespDto.OrderInfo.builder()
                .orderId(orderItem.getOrderId())
                .orderNum(orderItem.getOrderNum())
                .orderCreatedAt(orderItem.getCreatedAt())
                .paymentAt(orderItem.getPaymentAt())
                .deliveryStatus(orderItem.getDeliveryStatus())
                .build();
        
     // payments 테이블에서 결제 정보 조회
        Optional<Payments> paymentOpt = paymentsRepository.findByOrderId2(orderItem.getOrderId());
        
        MyOrderDetailRespDto.PaymentInfo.PaymentInfoBuilder paymentInfoBuilder = MyOrderDetailRespDto.PaymentInfo.builder()
                // OrderItem에서 가져오는 기본 금액 정보
                .totalPrice(orderItem.getTotalPrice())
                .deliveryPrice(orderItem.getDeliveryPrice())
                .totalDiscountPrice(orderItem.getTotalDiscountPrice())
                .finalPrice(orderItem.getFinalPrice())
                .usedMileage(orderItem.getUsedMileage())
                .deliveryStatusReason(orderItem.getDeliveryStatusReason());
        
        // payments 테이블 정보가 있으면 추가
        if (paymentOpt.isPresent()) {
            Payments payment = paymentOpt.get();
            
            paymentInfoBuilder
                .paymentMethod(payment.getPaymentMethod())
                .paymentAmount(payment.getPaymentAmount())
                .paymentStatus(payment.getPaymentStatus())
                .pgProvider(payment.getPgProvider())
                .paymentCreatedAt(payment.getCreatedAt())
                .paymentUpdatedAt(payment.getUpdatedAt());
                
            log.debug("결제 정보 조회 완료 - orderId: {}, method: {}, status: {}", 
                    orderItem.getOrderId(), payment.getPaymentMethod(), payment.getPaymentStatus());
        } else {
            // payments 정보가 없는 경우 기본값 설정
            paymentInfoBuilder
                .paymentMethod("정보없음")
                .paymentAmount(null)
                .paymentStatus("정보없음")
                .pgProvider("정보없음")
                .paymentCreatedAt(null)
                .paymentUpdatedAt(null);
                
            log.warn("결제 정보 없음 - orderId: {}", orderItem.getOrderId());
        }
        
        MyOrderDetailRespDto.PaymentInfo paymentInfo = paymentInfoBuilder.build();
        
        // 배송 정보
        MyOrderDetailRespDto.DeliveryInfo deliveryInfo = MyOrderDetailRespDto.DeliveryInfo.builder()
                .receivedUserName(orderItem.getReceivedUserName())
                .receivedUserPhone(orderItem.getReceivedUserPhone())
                .receivedUserZipCode(orderItem.getReceivedUserZipCode())
                .receivedUserAddress(orderItem.getReceivedUserAddress())
                .receivedUserAddress2(orderItem.getReceivedUserAddress2())
                .message(orderItem.getMessage())
                .build();
        
        // 주문 상품 목록
        List<MyOrderDetailRespDto.OrderItemDetail> orderItems = orderDetails.stream()
                .map(this::convertToOrderItemDetail)
                .collect(Collectors.toList());
        
        return MyOrderDetailRespDto.builder()
                .orderInfo(orderInfo)
                .paymentInfo(paymentInfo)
                .deliveryInfo(deliveryInfo)
                .orderItems(orderItems)
                .build();
    }
    
    /**
     * OrderDetail을 OrderItemDetail로 변환
     */
    private MyOrderDetailRespDto.OrderItemDetail convertToOrderItemDetail(OrderDetail orderDetail) {
        Product product = orderDetail.getProduct();
        
        // 대표 상품 이미지 1개 조회
        String productImgUrl = null;
        if (product != null && product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            productImgUrl = product.getProductImages().get(0).getProductImgUrl();
        }
        
        // 옵션명 조회
        String option1Name = null;
        String option2Name = null;
        
        if (orderDetail.hasOption1()) {
            option1Name = getOptionValueName(orderDetail.getOption1());
        }
        if (orderDetail.hasOption2()) {
            option2Name = getOptionValueName(orderDetail.getOption2());
        }
        
        // ===== changeStatus 조회 (추가됨) =====
        String changeStatus = null;
        Integer orderStatus = orderDetail.getOrderStatus();
        
        // orderStatus가 2(교환) 또는 3(반품)이면 change_item에서 changeStatus 조회
        if (orderStatus != null && (orderStatus == 2 || orderStatus == 3)) {
            Optional<ChangeItem> changeItemOpt = changeItemRepository.findByOrderDetailId(orderDetail.getOrderDetailId());
            if (changeItemOpt.isPresent()) {
                changeStatus = changeItemOpt.get().getChangeStatus();
                log.debug("changeStatus 조회 완료 - orderDetailId: {}, orderStatus: {}, changeStatus: {}", 
                        orderDetail.getOrderDetailId(), orderStatus, changeStatus);
            } else {
                // change_item에 데이터가 없는 경우 (데이터 정합성 문제)
                log.warn("orderStatus가 {0}이지만 change_item 데이터 없음 - orderDetailId: {}", 
                        orderStatus, orderDetail.getOrderDetailId());
                changeStatus = "정보없음";
            }
        }
        
        return MyOrderDetailRespDto.OrderItemDetail.builder()
                .orderDetailId(orderDetail.getOrderDetailId())
                .productId(orderDetail.getProductId())
                .productName(product != null ? product.getName() : "알 수 없는 상품")
                .orderStatus(orderDetail.getOrderStatus())
                .changeStatus(changeStatus)
                .productImgUrl(productImgUrl)
                
                // 옵션 정보
                .option1(orderDetail.getOption1())
                .option2(orderDetail.getOption2())
                .option1Name(option1Name)
                .option2Name(option2Name)
                
                // 가격 및 수량 정보
                .quantity(orderDetail.getQuantity())
                .basePrice(orderDetail.getBasePrice())
                .optionPrice(orderDetail.getOptionPrice())
                .lineTotalPrice(orderDetail.getLineTotalPrice())
                .build();
    }
    
    /**
     * 주문별 배송정보 조회
     */
    @Transactional
    public RespDto<DeliveryInfoRespDto> getDeliveryInfo(Integer orderId) {
        try {
            log.info("배송정보 조회 시작 - orderId: {}", orderId);
            
            // 주문 조회
            Optional<OrderItem> orderItemOpt = orderItemRepository.findByOrderId(orderId);
            if (orderItemOpt.isEmpty()) {
                log.warn("존재하지 않는 주문 - orderId: {}", orderId);
                return RespDto.<DeliveryInfoRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            OrderItem orderItem = orderItemOpt.get();
            
            // 배송정보 생성
            DeliveryInfoRespDto deliveryInfo = DeliveryInfoRespDto.of(
                    orderItem.getDeliveryCom(),
                    orderItem.getInvoiceNum()
            );
            
            log.info("배송정보 조회 완료 - orderId: {}, deliveryCom: {}, invoiceNum: {}", 
                    orderId, orderItem.getDeliveryCom(), orderItem.getInvoiceNum());
            
            return RespDto.<DeliveryInfoRespDto>builder()
                    .code(1)
                    .data(deliveryInfo)
                    .build();
            
        } catch (Exception e) {
            log.error("배송정보 조회 실패 - orderId: {}", orderId, e);
            return RespDto.<DeliveryInfoRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
}