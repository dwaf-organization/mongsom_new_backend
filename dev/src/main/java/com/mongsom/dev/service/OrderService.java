package com.mongsom.dev.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.order.reqDto.OrderCancelReqDto;
import com.mongsom.dev.dto.order.reqDto.OrderCreateReqDto;
import com.mongsom.dev.dto.order.reqDto.PaymentUpdateReqDto;
import com.mongsom.dev.dto.order.respDto.MileageRespDto;
import com.mongsom.dev.dto.order.respDto.OrderCancelRespDto;
import com.mongsom.dev.dto.order.respDto.OrderCreateRespDto;
import com.mongsom.dev.entity.OrderDetail;
import com.mongsom.dev.entity.OrderItem;
import com.mongsom.dev.entity.Payments;
import com.mongsom.dev.entity.Product;
import com.mongsom.dev.entity.User;
import com.mongsom.dev.repository.CartRepository;
import com.mongsom.dev.repository.OrderDetailRepository;
import com.mongsom.dev.repository.OrderItemRepository;
import com.mongsom.dev.repository.PaymentsRepository;
import com.mongsom.dev.repository.ProductRepository;
import com.mongsom.dev.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderItemRepository orderItemRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentsRepository paymentsRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    
    // 주문생성
    @Transactional
    public RespDto<OrderCreateRespDto> createOrder(OrderCreateReqDto reqDto) {
        try {
            log.info("주문 생성 시작 - userCode: {}, finalPrice: {}, 상품 수: {}", 
                    reqDto.getUserCode(), reqDto.getFinalPrice(), reqDto.getOrderDetails().size());
            
            // 1. 사용자 존재 확인
            Optional<User> userOpt = userRepository.findUserByUserCode(reqDto.getUserCode());
            if (userOpt.isEmpty()) {
                log.warn("존재하지 않는 사용자 - userCode: {}", reqDto.getUserCode());
                return RespDto.<OrderCreateRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            User user = userOpt.get();
            
            // 2. 마일리지 사용 가능 여부 확인
            if (reqDto.getUsedMileage() > 0 && !user.canUseMileage(reqDto.getUsedMileage())) {
                log.warn("마일리지 부족 - userCode: {}, 보유: {}, 사용요청: {}", 
                        reqDto.getUserCode(), user.getMileage(), reqDto.getUsedMileage());
                return RespDto.<OrderCreateRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            // 3. 상품 존재 확인
            for (OrderCreateReqDto.OrderDetailDto detail : reqDto.getOrderDetails()) {
                Optional<Product> productOpt = productRepository.findById(detail.getProductId());
                if (productOpt.isEmpty()) {
                    log.warn("존재하지 않는 상품 - productId: {}", detail.getProductId());
                    return RespDto.<OrderCreateRespDto>builder()
                            .code(-1)
                            .data(null)
                            .build();
                }
            }
            
            // 4. delivery_status_reason 설정
            String deliveryStatusReason = "CARD".equals(reqDto.getPaymentType()) ? "일반결제" : "무통장입금";
            
            // 5. 무통장입금인 경우 마일리지 즉시 차감
            if ("무통장입금".equals(deliveryStatusReason) && reqDto.getUsedMileage() > 0) {
                log.info("=== 무통장입금 마일리지 즉시 차감 시작 ===");
                
                boolean mileageDeducted = deductMileageForBankTransfer(user, reqDto.getUsedMileage());
                if (!mileageDeducted) {
                    log.error("무통장입금 마일리지 차감 실패 - userCode: {}, usedMileage: {}", 
                            reqDto.getUserCode(), reqDto.getUsedMileage());
                    return RespDto.<OrderCreateRespDto>builder()
                            .code(-1)
                            .data(null)
                            .build();
                }
                
                log.info("무통장입금 마일리지 차감 완료 - userCode: {}, 차감: {}", 
                        reqDto.getUserCode(), reqDto.getUsedMileage());
            } else {
                log.info("일반결제(카드) - 마일리지는 결제 승인 후 차감 예정");
            }
            
            // 6. OrderItem 생성 및 저장
            OrderItem orderItem = OrderItem.builder()
                    .userCode(reqDto.getUserCode())
                    .receivedUserName(reqDto.getReceivedUserName())
                    .receivedUserPhone(reqDto.getReceivedUserPhone())
                    .receivedUserZipCode(reqDto.getReceivedUserZipCode())
                    .receivedUserAddress(reqDto.getReceivedUserAddress())
                    .receivedUserAddress2(reqDto.getReceivedUserAddress2())
                    .message(reqDto.getMessage())
                    .totalPrice(reqDto.getTotalPrice())
                    .deliveryPrice(reqDto.getDeliveryPrice())
                    .totalDiscountPrice(reqDto.getTotalDiscountPrice())
                    .finalPrice(reqDto.getFinalPrice())
                    .usedMileage(reqDto.getUsedMileage())
                    .deliveryStatus("결제대기")  // 고정값
                    .deliveryStatusReason(deliveryStatusReason)
                    .paymentAt(LocalDateTime.now())
                    .orderNum("temp")
                    .build();
            
            OrderItem savedOrderItem = orderItemRepository.save(orderItem);
            Integer orderId = savedOrderItem.getOrderId();
            
            // 6-1. orderNum 생성 및 업데이트 (mongsom_orderId 형식)
            String orderNum = "mongsom_" + orderId;
            savedOrderItem.setOrderNum(orderNum);
            orderItemRepository.save(savedOrderItem);
            
            log.info("주문 기본 정보 저장 완료 - orderId: {}", orderId);
            
            // 6. OrderDetail 생성 및 저장 (새로운 구조)
            for (OrderCreateReqDto.OrderDetailDto detailDto : reqDto.getOrderDetails()) {
                OrderDetail orderDetail = OrderDetail.createOrderDetail(
                    orderId,
                    reqDto.getUserCode(),
                    detailDto.getProductId(),
                    detailDto.getOption1(),
                    detailDto.getOption2(),
                    detailDto.getQuantity(),
                    detailDto.getBasePrice(),
                    detailDto.getOptionPrice()
                );
                
                orderDetailRepository.save(orderDetail);
            }
            
            log.info("주문 상세 정보 저장 완료 - orderId: {}, 상품 수: {}", orderId, reqDto.getOrderDetails().size());
            
            // 7. Payments 생성 및 저장
            Payments payment = Payments.builder()
                    .orderId(orderId)
                    .userCode(reqDto.getUserCode())
                    .paymentMethod(null)
                    .paymentAmount(0)
                    .paymentStatus("대기중")
                    .paymentKey(null)
                    .pgProvider(null)
                    .build();
            
            paymentsRepository.save(payment);
            
            // 8. 마일리지 차감 (주문번호 생성이 목적이므로 결제 완료 시가 아닌 주문 생성 시에 차감하지 않음)
            
            log.info("주문 생성 완료 - orderId: {}, userCode: {}, finalPrice: {}", 
                    orderId, reqDto.getUserCode(), reqDto.getFinalPrice());
            
            OrderCreateRespDto responseData = OrderCreateRespDto.builder()
                    .orderNum(orderNum)
                    .finalPrice(reqDto.getFinalPrice())
                    .orderId(orderId)                    // 추가 정보
//                    .totalPrice(reqDto.getTotalPrice())   // 추가 정보
//                    .deliveryPrice(reqDto.getDeliveryPrice()) // 추가 정보
//                    .usedMileage(reqDto.getUsedMileage()) // 추가 정보
//                    .deliveryStatus("결제대기")           // 추가 정보
                    .build();
            
            // 9. 장바구니 삭제
            if ("무통장입금".equals(deliveryStatusReason)) {
                log.info("=== 무통장입금 장바구니 삭제 시작 ===");
                
                int deletedCartCount = cartRepository.deleteByUserCode(reqDto.getUserCode());
                log.info("장바구니 삭제 완료 - userCode: {}, 삭제된 항목 수: {}", 
                        reqDto.getUserCode(), deletedCartCount);
                
            } else {
                log.info("일반결제(카드) - 장바구니 삭제는 결제 승인 후 차감 예정");
            }
            
            return RespDto.<OrderCreateRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("주문 생성 실패 - userCode: {}, finalPrice: {}", 
                    reqDto.getUserCode(), reqDto.getFinalPrice(), e);
            return RespDto.<OrderCreateRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
   
    /**
     * 무통장입금용 마일리지 차감
     */
    private boolean deductMileageForBankTransfer(User user, Integer usedMileage) {
        try {
            log.info("무통장입금 마일리지 차감 - userCode: {}, 차감금액: {}", 
                    user.getUserCode(), usedMileage);
            
            Integer currentMileage = user.getMileage();
            
            // User 엔티티의 비즈니스 메서드로 마일리지 차감
            boolean deductSuccess = user.deductMileage(usedMileage);
            
            if (!deductSuccess) {
                log.error("마일리지 차감 실패 - userCode: {}, 보유: {}, 사용요청: {}", 
                        user.getUserCode(), currentMileage, usedMileage);
                return false;
            }
            
            // 변경된 사용자 정보 저장
            userRepository.save(user);
            
            Integer newMileage = user.getMileage();
            log.info("무통장입금 마일리지 차감 완료 - userCode: {}, 기존: {} → 변경: {} (차감: {})", 
                    user.getUserCode(), currentMileage, newMileage, usedMileage);
            
            return true;
            
        } catch (Exception e) {
            log.error("무통장입금 마일리지 차감 중 오류 발생", e);
            return false;
        }
    }
    
    // 주문취소
    @Transactional
    public RespDto<Boolean> cancelOrder(OrderCancelReqDto reqDto) {
        try {
            log.info("주문 취소 시작 - orderId: {}, userCode: {}, orderDetailId: {}", 
                    reqDto.getOrderId(), reqDto.getUserCode(), reqDto.getOrderDetailId());
            
            // 1. 사용자 존재 확인
            Optional<User> userOpt = userRepository.findUserByUserCode(reqDto.getUserCode());
            if (userOpt.isEmpty()) {
                log.warn("존재하지 않는 사용자 - userCode: {}", reqDto.getUserCode());
                return RespDto.<Boolean>builder()
                        .code(-1)
                        .data(false)
                        .build();
            }
            
            // 2. 주문 상세 존재 확인 및 권한 검증
            Optional<OrderDetail> orderDetailOpt = orderDetailRepository.findById(reqDto.getOrderDetailId());
            if (orderDetailOpt.isEmpty()) {
                log.warn("존재하지 않는 주문 상세 - orderDetailId: {}", reqDto.getOrderDetailId());
                return RespDto.<Boolean>builder()
                        .code(-1)
                        .data(false)
                        .build();
            }
            
            OrderDetail orderDetail = orderDetailOpt.get();
            
            // 3. 권한 검증 (주문자 본인인지 확인)
            if (!orderDetail.getOrderId().equals(reqDto.getOrderId())) {
                log.warn("주문 취소 권한 없음 - orderId: {}, orderDetailId: {}", 
                        reqDto.getOrderId(), reqDto.getOrderDetailId());
                return RespDto.<Boolean>builder()
                        .code(-1)
                        .data(false)
                        .build();
            }
            
            // 4. 이미 취소된 주문인지 확인
            if (orderDetail.getOrderStatus().equals(1)) {
                log.warn("이미 취소된 주문 - orderDetailId: {}", reqDto.getOrderDetailId());
                return RespDto.<Boolean>builder()
                        .code(-1)
                        .data(false)
                        .build();
            }
            
            // 5. 주문 상세 취소 처리 (order_status = 1)
            orderDetail.setOrderStatus(1);
            orderDetailRepository.save(orderDetail);
            
            log.info("주문 상세 취소 완료 - orderDetailId: {}", reqDto.getOrderDetailId());
            
            // 6. 해당 주문의 모든 상세 조회
            List<OrderDetail> allOrderDetails = orderDetailRepository.findByOrderIdOrderByCreatedAt(reqDto.getOrderId());
            
            // 7. 모든 상세가 취소되었는지 확인
            boolean allCancelled = allOrderDetails.stream()
                    .allMatch(detail -> detail.getOrderStatus().equals(1));
            
            if (allCancelled) {
                // 8. 모든 상세가 취소된 경우 주문 기본 정보의 배송 상태 변경
                Optional<OrderItem> orderItemOpt = orderItemRepository.findById(reqDto.getOrderId());
                if (orderItemOpt.isPresent()) {
                    OrderItem orderItem = orderItemOpt.get();
                    orderItem.setDeliveryStatus("주문취소");
                    orderItemRepository.save(orderItem);
                    
                    log.info("주문 전체 취소 완료 - orderId: {}, deliveryStatus: '주문취소'", reqDto.getOrderId());
                }
            } else {
                log.info("주문 부분 취소 완료 - orderId: {}, 취소된 상세: {}, 전체 상세: {}", 
                        reqDto.getOrderId(), 
                        allOrderDetails.stream().mapToInt(detail -> detail.getOrderStatus()).sum(),
                        allOrderDetails.size());
            }
            
            log.info("주문 취소 처리 완료 - orderId: {}, userCode: {}, orderDetailId: {}", 
                    reqDto.getOrderId(), reqDto.getUserCode(), reqDto.getOrderDetailId());
            
            return RespDto.<Boolean>builder()
                    .code(1)
                    .data(true)
                    .build();
            
        } catch (Exception e) {
            log.error("주문 취소 실패 - orderId: {}, userCode: {}, orderDetailId: {}", 
                    reqDto.getOrderId(), reqDto.getUserCode(), reqDto.getOrderDetailId(), e);
            return RespDto.<Boolean>builder()
                    .code(-1)
                    .data(false)
                    .build();
        }
    }
    
    //결제 정보 업데이트
    @Transactional
    public RespDto<String> updatePaymentInfo(PaymentUpdateReqDto reqDto) {
        try {
            log.info("=== 결제 정보 업데이트 시작 - orderId: {}, userCode: {}, paymentMethod: {} ===", 
                    reqDto.getOrderId(), reqDto.getUserCode(), reqDto.getPaymentMethod());
            
            // 1. 주문 존재 확인
            Optional<OrderItem> orderOpt = orderItemRepository.findById(reqDto.getOrderId());
            if (orderOpt.isEmpty()) {
                log.error("주문을 찾을 수 없습니다 - orderId: {}", reqDto.getOrderId());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("주문을 찾을 수 없습니다.")
                        .build();
            }
            
            OrderItem order = orderOpt.get();
            
            // 2. 사용자 권한 확인
            if (!order.getUserCode().equals(reqDto.getUserCode())) {
                log.error("주문 소유자가 아닙니다 - orderId: {}, requestUserCode: {}, orderUserCode: {}", 
                        reqDto.getOrderId(), reqDto.getUserCode(), order.getUserCode());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("해당 주문에 대한 권한이 없습니다.")
                        .build();
            }
            
            // 3. 기존 결제 정보 조회
            Optional<Payments> paymentOpt = paymentsRepository.findByOrderIdAndUserCode(
                    reqDto.getOrderId(), reqDto.getUserCode());
            
            if (paymentOpt.isEmpty()) {
                log.error("결제 정보를 찾을 수 없습니다 - orderId: {}, userCode: {}", 
                        reqDto.getOrderId(), reqDto.getUserCode());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("결제 정보를 찾을 수 없습니다.")
                        .build();
            }
            
            // 4. 결제 정보 업데이트
            int updatedRows = paymentsRepository.updatePaymentInfo(
                    reqDto.getOrderId(),
                    reqDto.getUserCode(),
                    reqDto.getPaymentMethod(),
                    reqDto.getPaymentStatus() != null ? reqDto.getPaymentStatus() : "PENDING",
                    reqDto.getPaymentKey(),
                    reqDto.getPgProvider()
            );
            
            // 4-1. 결제 완료 시 주문 상태 업데이트
            if ("COMPLETED".equals(reqDto.getPaymentStatus()) && updatedRows > 0) {
                int orderUpdatedRows = orderItemRepository.updateDeliveryStatus(
                    reqDto.getOrderId(), "결제완료");
                
                log.info("주문 상태 업데이트 완료 - orderId: {}, deliveryStatus: 결제완료, 업데이트된 행: {}", 
                    reqDto.getOrderId(), orderUpdatedRows);
                
                // 4-2. 마일리지 차감 (결제 완료 시)
                if (order.getUsedMileage() > 0) {
                    Optional<User> userOpt = userRepository.findUserByUserCode(reqDto.getUserCode());
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        user.useMileage(order.getUsedMileage());
                        userRepository.save(user);
                        
                        log.info("마일리지 차감 완료 - userCode: {}, 사용마일리지: {}, 잔여마일리지: {}", 
                                reqDto.getUserCode(), order.getUsedMileage(), user.getMileage());
                    }
                }
            }
            
            // 5. 결제 완료 시 장바구니 삭제
            if ("COMPLETED".equals(reqDto.getPaymentStatus())) {
                int deletedCartCount = cartRepository.deleteByUserCode(reqDto.getUserCode());
                log.info("장바구니 삭제 완료 - userCode: {}, 삭제된 항목 수: {}", 
                        reqDto.getUserCode(), deletedCartCount);
            }
            
            if (updatedRows > 0) {
                log.info("결제 정보 업데이트 완료 - orderId: {}, userCode: {}, paymentMethod: {}, paymentStatus: {}", 
                        reqDto.getOrderId(), reqDto.getUserCode(), reqDto.getPaymentMethod(), 
                        reqDto.getPaymentStatus());
                
                return RespDto.<String>builder()
                        .code(1)
                        .data("결제 정보가 성공적으로 업데이트되었습니다.")
                        .build();
            } else {
                log.error("결제 정보 업데이트 실패 - orderId: {}, userCode: {}", 
                        reqDto.getOrderId(), reqDto.getUserCode());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("결제 정보 업데이트에 실패했습니다.")
                        .build();
            }
            
        } catch (Exception e) {
            log.error("결제 정보 업데이트 실패 - orderId: {}, userCode: {}, error: {}", 
                    reqDto.getOrderId(), reqDto.getUserCode(), e.getMessage());
            return RespDto.<String>builder()
                    .code(-1)
                    .data("결제 정보 업데이트 중 오류가 발생했습니다.")
                    .build();
        }
    }
    
    /**
     * 사용자 마일리지 조회
     */
    public RespDto<MileageRespDto> getUserMileage(Long userCode) {
        try {
            log.info("사용자 마일리지 조회 시작 - userCode: {}", userCode);
            
            // 1. 사용자 존재 확인
            Optional<User> userOpt = userRepository.findUserByUserCode(userCode);
            if (userOpt.isEmpty()) {
                log.warn("존재하지 않는 사용자 - userCode: {}", userCode);
                return RespDto.<MileageRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            User user = userOpt.get();
            
            // 2. 마일리지 정보 반환
            MileageRespDto mileageInfo = MileageRespDto.from(
                user.getUserCode(),
                user.getMileage(),
                user.getName()
            );
            
            log.info("마일리지 조회 완료 - userCode: {}, mileage: {}", userCode, user.getMileage());
            
            return RespDto.<MileageRespDto>builder()
                    .code(1)
                    .data(mileageInfo)
                    .build();
            
        } catch (Exception e) {
            log.error("마일리지 조회 실패 - userCode: {}", userCode, e);
            
            return RespDto.<MileageRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * 주문 취소 (무통장입금 시 마일리지 환불 포함)
     */
    @Transactional
    public RespDto<OrderCancelRespDto> cancelOrder(Integer orderId) {
        try {
            log.info("주문취소 시작 - orderId: {}", orderId);
            
            // 1. OrderItem 조회 및 취소 가능 여부 확인
            Optional<OrderItem> orderItemOpt = orderItemRepository.findByOrderId(orderId);
            if (orderItemOpt.isEmpty()) {
                log.warn("존재하지 않는 주문 - orderId: {}", orderId);
                return RespDto.<OrderCancelRespDto>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            OrderItem orderItem = orderItemOpt.get();
            
            // 2. 취소 가능 상태 확인 (결제대기만 취소 가능)
            if (!"결제대기".equals(orderItem.getDeliveryStatus())) {
                log.warn("취소 불가능한 상태 - orderId: {}, deliveryStatus: {}", 
                        orderId, orderItem.getDeliveryStatus());
                return RespDto.<OrderCancelRespDto>builder()
                        .code(-2)
                        .data(null)
                        .build();
            }
            
            // 🔥 3. 무통장입금인 경우 마일리지 환불 처리
            boolean mileageRefunded = false;
            if ("무통장입금".equals(orderItem.getDeliveryStatusReason()) && orderItem.getUsedMileage() > 0) {
                log.info("=== 무통장입금 주문취소 - 마일리지 환불 시작 ===");
                
                mileageRefunded = refundMileageForCancelledOrder(orderItem.getUserCode(), orderItem.getUsedMileage());
                if (!mileageRefunded) {
                    log.error("마일리지 환불 실패 - orderId: {}, userCode: {}, usedMileage: {}", 
                            orderId, orderItem.getUserCode(), orderItem.getUsedMileage());
                    return RespDto.<OrderCancelRespDto>builder()
                            .code(-1)
                            .data(null)
                            .build();
                }
                
                log.info("무통장입금 마일리지 환불 완료 - orderId: {}, 환불: {}", 
                        orderId, orderItem.getUsedMileage());
            } else {
                log.info("일반결제 또는 마일리지 미사용 - 마일리지 환불 불필요");
            }
            
            // 4. 취소 처리 (데이터 삭제) - 기존 로직
            OrderCancelRespDto cancelResult = performOrderCancellation(orderItem);
            
            log.info("주문취소 완료 - orderId: {}, mileageRefunded: {}", orderId, mileageRefunded);
            
            return RespDto.<OrderCancelRespDto>builder()
                    .code(1)
                    .data(cancelResult)
                    .build();
            
        } catch (Exception e) {
            log.error("주문취소 실패 - orderId: {}", orderId, e);
            return RespDto.<OrderCancelRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * 주문 취소 시 마일리지 환불
     */
    private boolean refundMileageForCancelledOrder(Long userCode, Integer refundMileage) {
        try {
            log.info("주문취소 마일리지 환불 시작 - userCode: {}, 환불금액: {}", userCode, refundMileage);
            
            // 1. 사용자 조회
            Optional<User> userOpt = userRepository.findUserByUserCode(userCode);
            if (userOpt.isEmpty()) {
                log.error("사용자를 찾을 수 없음 - userCode: {}", userCode);
                return false;
            }
            
            User user = userOpt.get();
            Integer currentMileage = user.getMileage();
            
            // 2. 마일리지 환불 (적립)
            user.addMileage(refundMileage);  // User 엔티티의 addMileage 메서드 사용
            userRepository.save(user);
            
            Integer newMileage = user.getMileage();
            log.info("주문취소 마일리지 환불 완료 - userCode: {}, 기존: {} → 변경: {} (환불: {})", 
                    userCode, currentMileage, newMileage, refundMileage);
            
            return true;
            
        } catch (Exception e) {
            log.error("주문취소 마일리지 환불 중 오류 발생 - userCode: {}, refundMileage: {}", 
                    userCode, refundMileage, e);
            return false;
        }
    }
    
    /**
     * 주문 취소 실행 (데이터 삭제 처리)
     */
    private OrderCancelRespDto performOrderCancellation(OrderItem orderItem) {
        Integer orderId = orderItem.getOrderId();
        String orderNum = orderItem.getOrderNum();
        String previousStatus = orderItem.getDeliveryStatus();
        LocalDateTime canceledAt = LocalDateTime.now();
        
        // 1. OrderDetail 삭제 (주문 상품들)
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        int deletedOrderDetailsCount = orderDetails.size();
        
        log.info("OrderDetail 삭제 시작 - orderId: {}, 삭제 대상: {}건", orderId, deletedOrderDetailsCount);
        orderDetailRepository.deleteByOrderId(orderId);
        log.info("OrderDetail 삭제 완료 - orderId: {}", orderId);
        
        // 2. Payment 삭제 (있는 경우만)
        boolean paymentDeleted = false;
        Optional<Payments> paymentOpt = paymentsRepository.findByOrderId2(orderId);
        if (paymentOpt.isPresent()) {
            log.info("Payment 삭제 시작 - orderId: {}", orderId);
            paymentsRepository.deleteByOrderId(orderId);
            paymentDeleted = true;
            log.info("Payment 삭제 완료 - orderId: {}", orderId);
        } else {
            log.info("Payment 데이터 없음 - orderId: {}", orderId);
        }
        
        // 3. OrderItem 삭제 (마지막)
        log.info("OrderItem 삭제 시작 - orderId: {}", orderId);
        orderItemRepository.deleteByOrderId(orderId);
        log.info("OrderItem 삭제 완료 - orderId: {}", orderId);
        
        // 4. 응답 데이터 생성
        return OrderCancelRespDto.builder()
                .orderId(orderId)
                .orderNum(orderNum)
                .previousStatus(previousStatus)
                .canceledAt(canceledAt)
                .message("주문이 성공적으로 취소되었습니다.")
                .deletedOrderDetails(deletedOrderDetailsCount)
                .paymentDeleted(paymentDeleted)
                .build();
    }
    
}