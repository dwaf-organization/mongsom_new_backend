package com.mongsom.dev.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.admin.order.reqDto.AdminOrderSearchReqDto;
import com.mongsom.dev.dto.admin.order.respDto.AdminOrderListRespDto;
import com.mongsom.dev.entity.OrderItem;
import com.mongsom.dev.entity.OrderDetail;
import com.mongsom.dev.entity.Product;
import com.mongsom.dev.entity.User;
import com.mongsom.dev.entity.Payments;
import com.mongsom.dev.repository.OrderItemRepository;
import com.mongsom.dev.repository.OrderDetailRepository;
import com.mongsom.dev.repository.UserRepository;
import com.mongsom.dev.repository.PaymentsRepository;
import com.mongsom.dev.repository.ProductOptionValueRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService {
    
    private final OrderItemRepository orderItemRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final PaymentsRepository paymentsRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    
    /**
     * 관리자 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public RespDto<AdminOrderListRespDto> getAdminOrderList(AdminOrderSearchReqDto searchDto, Pageable pageable) {
        try {
            log.info("관리자 주문조회 시작 - startDate: {}, endDate: {}, keyword: {}, status: {}", 
                    searchDto.getStartDate(), searchDto.getEndDate(), 
                    searchDto.getSearchKeyword(), searchDto.getOrderStatus());
            
            // 주문 목록 조회 (검색 조건 포함)
            Page<OrderItem> orderItemPage = orderItemRepository.findAdminOrders(
                    searchDto.getStartDate(),
                    searchDto.getEndDate(),
                    searchDto.getSearchKeyword(),
                    searchDto.getOrderStatus(),
                    pageable
            );
            
            // DTO 변환
            List<AdminOrderListRespDto.AdminOrderItemDto> orders = orderItemPage.getContent()
                    .stream()
                    .map(this::convertToAdminOrderItemDto)
                    .collect(Collectors.toList());
            
            // 페이지 정보 생성
            AdminOrderListRespDto.PaginationDto pagination = AdminOrderListRespDto.PaginationDto.builder()
                    .currentPage(orderItemPage.getNumber())
                    .totalPage(orderItemPage.getTotalPages())
                    .size(orderItemPage.getSize())
                    .totalElements(orderItemPage.getTotalElements())
                    .hasNext(orderItemPage.hasNext())
                    .hasPrevious(orderItemPage.hasPrevious())
                    .build();
            
            // 응답 데이터 생성
            AdminOrderListRespDto responseData = AdminOrderListRespDto.builder()
                    .orders(orders)
                    .pagination(pagination)
                    .build();
            
            log.info("관리자 주문조회 완료 - 총 {}건", orderItemPage.getTotalElements());
            
            return RespDto.<AdminOrderListRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("관리자 주문조회 실패", e);
            return RespDto.<AdminOrderListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * OrderItem을 AdminOrderItemDto로 변환
     */
    private AdminOrderListRespDto.AdminOrderItemDto convertToAdminOrderItemDto(OrderItem orderItem) {
        // 1. 주문자 정보 조회 (user_mst)
        String orderUser = "알 수 없는 사용자";
        Optional<User> userOpt = userRepository.findByUserCode(orderItem.getUserCode());
        if (userOpt.isPresent()) {
            orderUser = userOpt.get().getName();
        }
        
        // 2. 상품정보 조회
        AdminOrderListRespDto.ProductInfoDto productInfo = buildProductInfo(orderItem);
        
        // 3. 결제상태 조회 (payments 테이블)
        String paymentStatus = "정보없음";
        Optional<Payments> paymentOpt = paymentsRepository.findByOrderId2(orderItem.getOrderId());
        if (paymentOpt.isPresent()) {
            paymentStatus = paymentOpt.get().getPaymentStatus();
        }
        
        return AdminOrderListRespDto.AdminOrderItemDto.builder()
                .paymentAt(orderItem.getPaymentAt())
                .orderId(orderItem.getOrderId())
                .orderNum(orderItem.getOrderNum())
                .userCode(userOpt.get().getUserCode())
                .orderUser(orderUser)
                .productInfo(productInfo)
                .finalPrice(orderItem.getFinalPrice())
                .paymentStatus(paymentStatus)
                .deliveryStatus(orderItem.getDeliveryStatus())
                .invoiceNum(orderItem.getInvoiceNum())
                .build();
    }
    
    /**
     * 상품정보 구성 (첫 번째 상품 + 외 N개)
     */
    private AdminOrderListRespDto.ProductInfoDto buildProductInfo(OrderItem orderItem) {
        // 주문의 모든 상품 조회
        List<OrderDetail> orderDetails = orderDetailRepository
                .findByOrderIdOrderByOrderDetailIdAsc(orderItem.getOrderId());
        
        if (orderDetails.isEmpty()) {
            return AdminOrderListRespDto.ProductInfoDto.builder()
                    .productName("상품정보 없음")
                    .productImgUrl(null)
                    .optionSummary(null)
                    .build();
        }
        
        // 첫 번째 상품 정보
        OrderDetail firstOrderDetail = orderDetails.get(0);
        Product firstProduct = firstOrderDetail.getProduct();
        
        // 상품명 생성 (외 N개 포함)
        String productName = firstProduct != null ? firstProduct.getName() : "알 수 없는 상품";
        int totalProductCount = orderDetails.size();
        if (totalProductCount > 1) {
            productName += " 외 " + (totalProductCount - 1) + "개";
        }
        
        // 대표 상품 이미지
        String productImgUrl = null;
        if (firstProduct != null && firstProduct.getProductImages() != null && 
            !firstProduct.getProductImages().isEmpty()) {
            productImgUrl = firstProduct.getProductImages().get(0).getProductImgUrl();
        }
        
        // 옵션 요약 생성
        String optionSummary = buildOptionSummary(firstOrderDetail);
        
        return AdminOrderListRespDto.ProductInfoDto.builder()
                .productName(productName)
                .productImgUrl(productImgUrl)
                .optionSummary(optionSummary)
                .build();
    }
    
    /**
     * 옵션 요약 생성 (500ml, 블랙)
     */
    private String buildOptionSummary(OrderDetail orderDetail) {
        List<String> optionParts = new ArrayList<>();
        
        // 옵션1 이름 조회
        if (orderDetail.hasOption1()) {
            String option1Name = getOptionValueName(orderDetail.getOption1());
            if (option1Name != null) {
                optionParts.add(option1Name);
            }
        }
        
        // 옵션2 이름 조회
        if (orderDetail.hasOption2()) {
            String option2Name = getOptionValueName(orderDetail.getOption2());
            if (option2Name != null) {
                optionParts.add(option2Name);
            }
        }
        
        // 옵션이 없으면 null 반환
        if (optionParts.isEmpty()) {
            return null;
        }
        
        // 콤마로 조합
        return String.join(", ", optionParts);
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
}