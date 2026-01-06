package com.mongsom.dev.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.admin.change.reqDto.AdminChangeStatusUpdateReqDto;
import com.mongsom.dev.dto.admin.change.respDto.AdminChangeDetailRespDto;
import com.mongsom.dev.dto.admin.change.respDto.AdminChangeListRespDto;
import com.mongsom.dev.dto.admin.change.respDto.AdminChangeStatusUpdateRespDto;
import com.mongsom.dev.entity.ChangeItem;
import com.mongsom.dev.entity.OrderItem;
import com.mongsom.dev.entity.Payments;
import com.mongsom.dev.entity.OrderDetail;
import com.mongsom.dev.entity.Product;
import com.mongsom.dev.entity.User;
import com.mongsom.dev.repository.ChangeItemRepository;
import com.mongsom.dev.repository.OrderItemRepository;
import com.mongsom.dev.repository.PaymentsRepository;
import com.mongsom.dev.repository.OrderDetailRepository;
import com.mongsom.dev.repository.ProductOptionValueRepository;
import com.mongsom.dev.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminChangeService {
    
    private final ChangeItemRepository changeItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    private final PaymentsRepository paymentsRepository;
    private final UserRepository userRepository;
    
    /**
     * 관리자 교환/반품 목록 조회
     */
    @Transactional(readOnly = true)
    public RespDto<AdminChangeListRespDto> getAdminChangeList(Integer changeStatus, Pageable pageable) {
        try {
            log.info("관리자 교환/반품 조회 시작 - changeStatus: {}, page: {}, size: {}", 
                    changeStatus, pageable.getPageNumber(), pageable.getPageSize());
            
            // changeStatus를 changeType으로 변환
            String changeType = getChangeTypeFromStatus(changeStatus);
            if (changeType == null) {
                log.warn("잘못된 changeStatus 값 - changeStatus: {}", changeStatus);
                return RespDto.<AdminChangeListRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            // 교환/반품 목록 조회
            Page<ChangeItem> changeItemPage = changeItemRepository.findAdminChangeList(changeType, pageable);
            
            // DTO 변환
            List<AdminChangeListRespDto.AdminChangeItemDto> changes = changeItemPage.getContent()
                    .stream()
                    .map(this::convertToAdminChangeItemDto)
                    .collect(Collectors.toList());
            
            // 페이지 정보 생성
            AdminChangeListRespDto.PaginationDto pagination = AdminChangeListRespDto.PaginationDto.builder()
                    .currentPage(changeItemPage.getNumber())
                    .totalPage(changeItemPage.getTotalPages())
                    .size(changeItemPage.getSize())
                    .totalElements(changeItemPage.getTotalElements())
                    .hasNext(changeItemPage.hasNext())
                    .hasPrevious(changeItemPage.hasPrevious())
                    .build();
            
            // 응답 데이터 생성
            AdminChangeListRespDto responseData = AdminChangeListRespDto.builder()
                    .changes(changes)
                    .pagination(pagination)
                    .build();
            
            log.info("관리자 교환/반품 조회 완료 - type: {}, 총 {}건", changeType, changeItemPage.getTotalElements());
            
            return RespDto.<AdminChangeListRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("관리자 교환/반품 조회 실패 - changeStatus: {}", changeStatus, e);
            return RespDto.<AdminChangeListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * changeStatus를 changeType으로 변환
     */
    private String getChangeTypeFromStatus(Integer changeStatus) {
        if (changeStatus == null) return null;
        
        switch (changeStatus) {
            case 1: return "교환";
            case 2: return "반품";
            default: return null;
        }
    }
    
    /**
     * ChangeItem을 AdminChangeItemDto로 변환
     */
    private AdminChangeListRespDto.AdminChangeItemDto convertToAdminChangeItemDto(ChangeItem changeItem) {
        // 1. OrderItem 조회
        Optional<OrderItem> orderItemOpt = orderItemRepository.findByOrderId(changeItem.getOrderId());
        if (orderItemOpt.isEmpty()) {
            log.warn("연관된 주문을 찾을 수 없음 - orderId: {}", changeItem.getOrderId());
            return createEmptyChangeItemDto(changeItem);
        }
        
        OrderItem orderItem = orderItemOpt.get();
        
        // 2. OrderDetail 조회
        Optional<OrderDetail> orderDetailOpt = orderDetailRepository.findById(changeItem.getOrderDetailId());
        if (orderDetailOpt.isEmpty()) {
            log.warn("연관된 주문 상세를 찾을 수 없음 - orderDetailId: {}", changeItem.getOrderDetailId());
            return createEmptyChangeItemDto(changeItem);
        }
        
        OrderDetail orderDetail = orderDetailOpt.get();
        
        // 3. 상품정보 구성
        AdminChangeListRespDto.ProductInfoDto productInfo = buildProductInfo(orderDetail);
        
        return AdminChangeListRespDto.AdminChangeItemDto.builder()
                .requestedAt(changeItem.getRequestedAt())
                .orderNum(orderItem.getOrderNum())
                .receivedUserName(orderItem.getReceivedUserName())
                .productInfo(productInfo)
                .finalPrice(orderItem.getFinalPrice())
                .changeStatus(changeItem.getChangeStatus())
                .changeId(changeItem.getChangeId())
                .orderId(changeItem.getOrderId())
                .orderDetailId(changeItem.getOrderDetailId())
                .build();
    }
    
    /**
     * 빈 ChangeItemDto 생성 (오류 케이스용)
     */
    private AdminChangeListRespDto.AdminChangeItemDto createEmptyChangeItemDto(ChangeItem changeItem) {
        return AdminChangeListRespDto.AdminChangeItemDto.builder()
                .requestedAt(changeItem.getRequestedAt())
                .orderNum("정보없음")
                .receivedUserName("정보없음")
                .productInfo(AdminChangeListRespDto.ProductInfoDto.builder()
                        .productName("정보없음")
                        .productImgUrl(null)
                        .option1(null)
                        .option2(null)
                        .option1Name(null)
                        .option2Name(null)
                        .optionComb("정보없음")
                        .build())
                .finalPrice(0)
                .changeStatus(changeItem.getChangeStatus())
                .changeId(changeItem.getChangeId())
                .orderId(changeItem.getOrderId())
                .orderDetailId(changeItem.getOrderDetailId())
                .build();
    }
    
    /**
     * 상품정보 구성
     */
    private AdminChangeListRespDto.ProductInfoDto buildProductInfo(OrderDetail orderDetail) {
        Product product = orderDetail.getProduct();
        
        // 상품명
        String productName = product != null ? product.getName() : "알 수 없는 상품";
        
        // 대표 상품 이미지 (첫 번째 이미지)
        String productImgUrl = null;
        if (product != null && product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            productImgUrl = product.getProductImages().get(0).getProductImgUrl();
        }
        
        // 옵션 정보 조회
        Integer option1 = orderDetail.getOption1();
        Integer option2 = orderDetail.getOption2();
        String option1Name = null;
        String option2Name = null;
        
        if (option1 != null) {
            option1Name = getOptionValueName(option1);
        }
        if (option2 != null) {
            option2Name = getOptionValueName(option2);
        }
        
        // 옵션 조합 생성
        String optionComb = buildOptionCombination(option1Name, option2Name);
        
        return AdminChangeListRespDto.ProductInfoDto.builder()
                .productName(productName)
                .productImgUrl(productImgUrl)
                .option1(option1)
                .option2(option2)
                .option1Name(option1Name)
                .option2Name(option2Name)
                .optionComb(optionComb)
                .build();
    }
    
    /**
     * 옵션 조합 생성 (500ml, 블랙)
     */
    private String buildOptionCombination(String option1Name, String option2Name) {
        List<String> optionParts = new ArrayList<>();
        
        if (option1Name != null && !option1Name.isEmpty()) {
            optionParts.add(option1Name);
        }
        if (option2Name != null && !option2Name.isEmpty()) {
            optionParts.add(option2Name);
        }
        
        if (optionParts.isEmpty()) {
            return null;
        }
        
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
    
    /**
     * 관리자 교환/반품 상세 조회
     */
    @Transactional(readOnly = true)
    public RespDto<AdminChangeDetailRespDto> getAdminChangeDetail(Integer changeId) {
        try {
            log.info("관리자 교환/반품 상세조회 시작 - changeId: {}", changeId);
            
            // 1. ChangeItem 조회
            Optional<ChangeItem> changeItemOpt = changeItemRepository.findById(changeId);
            if (changeItemOpt.isEmpty()) {
                log.warn("존재하지 않는 교환/반품 - changeId: {}", changeId);
                return RespDto.<AdminChangeDetailRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            ChangeItem changeItem = changeItemOpt.get();
            
            // 2. 관련 데이터 조회 및 DTO 변환
            AdminChangeDetailRespDto responseData = convertToAdminChangeDetailRespDto(changeItem);
            
            log.info("관리자 교환/반품 상세조회 완료 - changeId: {}", changeId);
            
            return RespDto.<AdminChangeDetailRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("관리자 교환/반품 상세조회 실패 - changeId: {}", changeId, e);
            return RespDto.<AdminChangeDetailRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * ChangeItem을 AdminChangeDetailRespDto로 변환
     */
    private AdminChangeDetailRespDto convertToAdminChangeDetailRespDto(ChangeItem changeItem) {
        // 1. 교환/반품 정보
        AdminChangeDetailRespDto.ChangeInfo changeInfo = buildChangeInfo(changeItem);
        
        // 2. 주문 정보
        AdminChangeDetailRespDto.OrderInfo orderInfo = buildOrderInfo(changeItem);
        
        // 3. 결제 정보
        AdminChangeDetailRespDto.PaymentInfo paymentInfo = buildPaymentInfo(changeItem);
        
        // 4. 사용자 정보
        AdminChangeDetailRespDto.UserInfo userInfo = buildUserInfo(changeItem);
        
        // 5. 배송 정보
        AdminChangeDetailRespDto.DeliveryInfo deliveryInfo = buildDeliveryInfo(changeItem);
        
        // 6. 상품 정보
        AdminChangeDetailRespDto.ProductInfo productInfo = buildProductInfo(changeItem);
        
        return AdminChangeDetailRespDto.builder()
                .changeInfo(changeInfo)
                .orderInfo(orderInfo)
                .paymentInfo(paymentInfo)
                .userInfo(userInfo)
                .deliveryInfo(deliveryInfo)
                .productInfo(productInfo)
                .build();
    }
    
    /**
     * 교환/반품 정보 구성
     */
    private AdminChangeDetailRespDto.ChangeInfo buildChangeInfo(ChangeItem changeItem) {
        return AdminChangeDetailRespDto.ChangeInfo.builder()
                .changeId(changeItem.getChangeId())
                .changeType(changeItem.getChangeType())
                .changeStatus(changeItem.getChangeStatus())
                .reason(changeItem.getReason())
                .refundBank(changeItem.getRefundBank())
                .refundAccount(changeItem.getRefundAccount())
                .requestedAt(changeItem.getRequestedAt())
                .build();
    }
    
    /**
     * 주문 정보 구성
     */
    private AdminChangeDetailRespDto.OrderInfo buildOrderInfo(ChangeItem changeItem) {
        Optional<OrderItem> orderItemOpt = orderItemRepository.findByOrderId(changeItem.getOrderId());
        
        if (orderItemOpt.isEmpty()) {
            log.warn("연관된 주문을 찾을 수 없음 - orderId: {}", changeItem.getOrderId());
            return AdminChangeDetailRespDto.OrderInfo.builder()
                    .orderId(changeItem.getOrderId())
                    .orderNum("정보없음")
                    .orderCreatedAt(null)
                    .paymentAt(null)
                    .deliveryStatus("정보없음")
                    .build();
        }
        
        OrderItem orderItem = orderItemOpt.get();
        
        return AdminChangeDetailRespDto.OrderInfo.builder()
                .orderId(orderItem.getOrderId())
                .orderNum(orderItem.getOrderNum())
                .orderCreatedAt(orderItem.getCreatedAt())
                .paymentAt(orderItem.getPaymentAt())
                .deliveryStatus(orderItem.getDeliveryStatus())
                .build();
    }
    
    /**
     * 결제 정보 구성
     */
    private AdminChangeDetailRespDto.PaymentInfo buildPaymentInfo(ChangeItem changeItem) {
        Optional<OrderItem> orderItemOpt = orderItemRepository.findByOrderId(changeItem.getOrderId());
        
        if (orderItemOpt.isEmpty()) {
            return AdminChangeDetailRespDto.PaymentInfo.builder()
                    .totalPrice(0)
                    .deliveryPrice(0)
                    .finalPrice(0)
                    .usedMileage(0)
                    .paymentMethod("정보없음")
                    .paymentStatus("정보없음")
                    .build();
        }
        
        OrderItem orderItem = orderItemOpt.get();
        
        // payments 테이블에서 결제 정보 조회
        Optional<Payments> paymentOpt = paymentsRepository.findByOrderId2(changeItem.getOrderId());
        String paymentMethod = "정보없음";
        String paymentStatus = "정보없음";
        
        if (paymentOpt.isPresent()) {
            Payments payment = paymentOpt.get();
            paymentMethod = payment.getPaymentMethod();
            paymentStatus = payment.getPaymentStatus();
        }
        
        return AdminChangeDetailRespDto.PaymentInfo.builder()
                .totalPrice(orderItem.getTotalPrice())
                .deliveryPrice(orderItem.getDeliveryPrice())
                .finalPrice(orderItem.getFinalPrice())
                .usedMileage(orderItem.getUsedMileage())
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .build();
    }
    
    /**
     * 사용자 정보 구성
     */
    private AdminChangeDetailRespDto.UserInfo buildUserInfo(ChangeItem changeItem) {
        Optional<User> userOpt = userRepository.findByUserCode(changeItem.getUserCode());
        
        if (userOpt.isEmpty()) {
            log.warn("연관된 사용자를 찾을 수 없음 - userCode: {}", changeItem.getUserCode());
            return AdminChangeDetailRespDto.UserInfo.builder()
                    .userCode(changeItem.getUserCode())
                    .userName("정보없음")
                    .userPhone("정보없음")
                    .userEmail("정보없음")
                    .build();
        }
        
        User user = userOpt.get();
        
        return AdminChangeDetailRespDto.UserInfo.builder()
                .userCode(user.getUserCode())
                .userName(user.getName())
                .userPhone(user.getPhone())
                .userEmail(user.getEmail())
                .build();
    }
    
    /**
     * 배송 정보 구성
     */
    private AdminChangeDetailRespDto.DeliveryInfo buildDeliveryInfo(ChangeItem changeItem) {
        Optional<OrderItem> orderItemOpt = orderItemRepository.findByOrderId(changeItem.getOrderId());
        
        if (orderItemOpt.isEmpty()) {
            return AdminChangeDetailRespDto.DeliveryInfo.builder()
                    .receivedUserName("정보없음")
                    .receivedUserPhone("정보없음")
                    .receivedUserZipCode("정보없음")
                    .receivedUserAddress("정보없음")
                    .receivedUserAddress2("정보없음")
                    .build();
        }
        
        OrderItem orderItem = orderItemOpt.get();
        
        return AdminChangeDetailRespDto.DeliveryInfo.builder()
                .receivedUserName(orderItem.getReceivedUserName())
                .receivedUserPhone(orderItem.getReceivedUserPhone())
                .receivedUserZipCode(orderItem.getReceivedUserZipCode())
                .receivedUserAddress(orderItem.getReceivedUserAddress())
                .receivedUserAddress2(orderItem.getReceivedUserAddress2())
                .build();
    }
    
    /**
     * 상품 정보 구성
     */
    private AdminChangeDetailRespDto.ProductInfo buildProductInfo(ChangeItem changeItem) {
        Optional<OrderDetail> orderDetailOpt = orderDetailRepository.findById(changeItem.getOrderDetailId());
        
        if (orderDetailOpt.isEmpty()) {
            log.warn("연관된 주문 상세를 찾을 수 없음 - orderDetailId: {}", changeItem.getOrderDetailId());
            return AdminChangeDetailRespDto.ProductInfo.builder()
                    .orderDetailId(changeItem.getOrderDetailId())
                    .productId(null)
                    .productName("정보없음")
                    .productImgUrls(new ArrayList<>())
                    .option1(null)
                    .option2(null)
                    .option1Name(null)
                    .option2Name(null)
                    .optionComb("정보없음")
                    .quantity(0)
                    .lineTotalPrice(0)
                    .orderStatus(null)
                    .build();
        }
        
        OrderDetail orderDetail = orderDetailOpt.get();
        Product product = orderDetail.getProduct();
        
        // 상품 이미지 목록
        List<String> productImgUrls = new ArrayList<>();
        if (product != null && product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            productImgUrls = product.getProductImages()
                    .stream()
                    .map(productImage -> productImage.getProductImgUrl())
                    .collect(Collectors.toList());
        }
        
        // 옵션 정보 조회
        String option1Name = null;
        String option2Name = null;
        
        if (orderDetail.getOption1() != null) {
            option1Name = getOptionValueName(orderDetail.getOption1());
        }
        if (orderDetail.getOption2() != null) {
            option2Name = getOptionValueName(orderDetail.getOption2());
        }
        
        // 옵션 조합 생성
        String optionComb = buildOptionCombination(option1Name, option2Name);
        
        return AdminChangeDetailRespDto.ProductInfo.builder()
                .orderDetailId(orderDetail.getOrderDetailId())
                .productId(orderDetail.getProductId())
                .productName(product != null ? product.getName() : "알 수 없는 상품")
                .productImgUrls(productImgUrls)
                .option1(orderDetail.getOption1())
                .option2(orderDetail.getOption2())
                .option1Name(option1Name)
                .option2Name(option2Name)
                .optionComb(optionComb)
                .quantity(orderDetail.getQuantity())
                .lineTotalPrice(orderDetail.getLineTotalPrice())
                .orderStatus(orderDetail.getOrderStatus())
                .build();
    }
    
    /**
     * 관리자 교환/반품 상태 변경
     */
    @Transactional
    public RespDto<AdminChangeStatusUpdateRespDto> updateChangeStatus(AdminChangeStatusUpdateReqDto reqDto) {
        try {
            log.info("교환/반품 상태 변경 시작 - changeId: {}, newStatus: {}", 
                    reqDto.getChangeId(), reqDto.getNewStatus());
            
            // 1. ChangeItem 조회
            Optional<ChangeItem> changeItemOpt = changeItemRepository.findById(reqDto.getChangeId());
            if (changeItemOpt.isEmpty()) {
                log.warn("존재하지 않는 교환/반품 - changeId: {}", reqDto.getChangeId());
                return RespDto.<AdminChangeStatusUpdateRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            ChangeItem changeItem = changeItemOpt.get();
            String previousStatus = changeItem.getChangeStatus();
            
            // 2. 상태 변경 유효성 검증
            String validationResult = validateStatusChange(changeItem, reqDto.getNewStatus());
            if (validationResult != null) {
                log.warn("상태 변경 불가 - changeId: {}, 사유: {}", reqDto.getChangeId(), validationResult);
                return RespDto.<AdminChangeStatusUpdateRespDto>builder()
                        .code(-2)
                        .data(null)
                        .build();
            }
            
            // 3. 상태 변경 처리
            LocalDateTime processedAt = LocalDateTime.now();
            changeItem.setChangeStatus(reqDto.getNewStatus());
            changeItem.setProcessedAt(processedAt);
            
            ChangeItem updatedChangeItem = changeItemRepository.save(changeItem);
            
            // 4. 응답 데이터 생성
            AdminChangeStatusUpdateRespDto responseData = AdminChangeStatusUpdateRespDto.builder()
                    .changeId(updatedChangeItem.getChangeId())
                    .previousStatus(previousStatus)
                    .newStatus(updatedChangeItem.getChangeStatus())
                    .processedAt(processedAt)
                    .message("상태가 성공적으로 변경되었습니다.")
                    .build();
            
            log.info("교환/반품 상태 변경 완료 - changeId: {}, {} → {}", 
                    reqDto.getChangeId(), previousStatus, reqDto.getNewStatus());
            
            return RespDto.<AdminChangeStatusUpdateRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("교환/반품 상태 변경 실패 - changeId: {}", reqDto.getChangeId(), e);
            return RespDto.<AdminChangeStatusUpdateRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * 상태 변경 유효성 검증
     */
    private String validateStatusChange(ChangeItem changeItem, String newStatus) {
        // 현재 상태와 동일한 상태로 변경하는 경우
        if (changeItem.getChangeStatus().equals(newStatus)) {
            return "이미 해당 상태입니다.";
        }
        
        // 교환/반품 타입과 상태 일치성 검증
        String changeType = changeItem.getChangeType();
        
        if ("교환".equals(changeType)) {
            // 교환 타입인데 반품 상태로 변경하려는 경우
            if (newStatus.startsWith("반품")) {
                return "교환 신청은 교환 관련 상태로만 변경 가능합니다.";
            }
        } else if ("반품".equals(changeType)) {
            // 반품 타입인데 교환 상태로 변경하려는 경우
            if (newStatus.startsWith("교환")) {
                return "반품 신청은 반품 관련 상태로만 변경 가능합니다.";
            }
        }
        
        // 유효성 검증 통과
        return null;
    }
    
}