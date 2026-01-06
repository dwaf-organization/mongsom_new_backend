package com.mongsom.dev.service.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.admin.delivery.reqDto.AdminDeliveryUpdateReqDto;
import com.mongsom.dev.dto.admin.delivery.respDto.AdminDeliveryUpdateRespDto;
import com.mongsom.dev.entity.OrderItem;
import com.mongsom.dev.entity.Payments;
import com.mongsom.dev.repository.OrderItemRepository;
import com.mongsom.dev.repository.PaymentsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDeliveryService {
    
    private final OrderItemRepository orderItemRepository;
    private final PaymentsRepository paymentsRepository;
    
    /**
     * 배송정보 일괄 업데이트
     */
    @Transactional
    public RespDto<AdminDeliveryUpdateRespDto> updateDeliveryInfoBatch(AdminDeliveryUpdateReqDto reqDto) {
        try {
            log.info("배송정보 일괄 업데이트 시작 - 총 {}건", reqDto.getDeliveryUpdates().size());
            
            List<AdminDeliveryUpdateRespDto.UpdateResultDto> results = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            
            // 각 배송정보 업데이트 처리
            for (AdminDeliveryUpdateReqDto.DeliveryUpdateItemDto item : reqDto.getDeliveryUpdates()) {
                AdminDeliveryUpdateRespDto.UpdateResultDto result = processDeliveryUpdate(item);
                results.add(result);
                
                if (result.getSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }
            
            // 배치 결과 생성
            AdminDeliveryUpdateRespDto.BatchResultDto batchResult = AdminDeliveryUpdateRespDto.BatchResultDto.builder()
                    .totalCount(reqDto.getDeliveryUpdates().size())
                    .successCount(successCount)
                    .failureCount(failureCount)
                    .summary(String.format("전체 %d건 중 성공 %d건, 실패 %d건", 
                            reqDto.getDeliveryUpdates().size(), successCount, failureCount))
                    .build();
            
            // 응답 데이터 생성
            AdminDeliveryUpdateRespDto responseData = AdminDeliveryUpdateRespDto.builder()
                    .batchResult(batchResult)
                    .results(results)
                    .build();
            
            log.info("배송정보 일괄 업데이트 완료 - 성공: {}건, 실패: {}건", successCount, failureCount);
            
            return RespDto.<AdminDeliveryUpdateRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("배송정보 일괄 업데이트 실패", e);
            return RespDto.<AdminDeliveryUpdateRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * 개별 배송정보 업데이트 처리
     */
    private AdminDeliveryUpdateRespDto.UpdateResultDto processDeliveryUpdate(
            AdminDeliveryUpdateReqDto.DeliveryUpdateItemDto item) {
        try {
            log.debug("배송정보 업데이트 처리 시작 - orderId: {}", item.getOrderId());
            
            // 1. OrderItem 조회
            Optional<OrderItem> orderItemOpt = orderItemRepository.findByOrderId(item.getOrderId());
            if (orderItemOpt.isEmpty()) {
                log.warn("존재하지 않는 주문 - orderId: {}", item.getOrderId());
                return AdminDeliveryUpdateRespDto.UpdateResultDto.builder()
                        .orderId(item.getOrderId())
                        .success(false)
                        .message("존재하지 않는 주문입니다.")
                        .bankTransferProcessed(false)
                        .build();
            }
            
            OrderItem orderItem = orderItemOpt.get();
            boolean bankTransferProcessed = false;
            
            // 2. 배송정보 기본 업데이트
            orderItem.setDeliveryStatus(item.getDeliveryStatus());
            if (item.getDeliveryCom() != null) {
                orderItem.setDeliveryCom(item.getDeliveryCom());
            }
            if (item.getInvoiceNum() != null) {
                orderItem.setInvoiceNum(item.getInvoiceNum());
            }
            
            // 3. 무통장입금 특별 처리 로직
            if ("결제완료".equals(item.getDeliveryStatus()) && 
                "무통장입금".equals(orderItem.getDeliveryStatusReason())) {
                
                log.info("무통장입금 처리 시작 - orderId: {}", item.getOrderId());
                
                // 3-1. payment_at 현재 시간으로 업데이트
                orderItem.setPaymentAt(LocalDateTime.now());
                
                // 3-2. payments 테이블 처리
                processBankTransferPayment(orderItem);
                bankTransferProcessed = true;
                
                log.info("무통장입금 처리 완료 - orderId: {}", item.getOrderId());
            }
            
            // 4. OrderItem 저장
            orderItemRepository.save(orderItem);
            
            log.debug("배송정보 업데이트 완료 - orderId: {}, status: {}", 
                    item.getOrderId(), item.getDeliveryStatus());
            
            String message = bankTransferProcessed ? 
                    "배송정보가 업데이트되었습니다. (무통장입금 처리 완료)" : 
                    "배송정보가 업데이트되었습니다.";
            
            return AdminDeliveryUpdateRespDto.UpdateResultDto.builder()
                    .orderId(item.getOrderId())
                    .success(true)
                    .message(message)
                    .bankTransferProcessed(bankTransferProcessed)
                    .build();
            
        } catch (Exception e) {
            log.error("배송정보 업데이트 실패 - orderId: {}", item.getOrderId(), e);
            return AdminDeliveryUpdateRespDto.UpdateResultDto.builder()
                    .orderId(item.getOrderId())
                    .success(false)
                    .message("업데이트 중 오류가 발생했습니다: " + e.getMessage())
                    .bankTransferProcessed(false)
                    .build();
        }
    }
    
    /**
     * 무통장입금 payments 테이블 처리
     */
    private void processBankTransferPayment(OrderItem orderItem) {
        try {
            Optional<Payments> existingPaymentOpt = paymentsRepository.findByOrderId2(orderItem.getOrderId());
            
            if (existingPaymentOpt.isPresent()) {
                // 기존 Payment 업데이트
                Payments existingPayment = existingPaymentOpt.get();
                existingPayment.setPaymentMethod("계좌이체");
                existingPayment.setPaymentAmount(orderItem.getFinalPrice());
                existingPayment.setPaymentStatus("결제완료");
                existingPayment.setPaymentKey(null);
                existingPayment.setPgProvider(null);
                
                paymentsRepository.save(existingPayment);
                
                log.debug("기존 Payment 업데이트 완료 - orderId: {}", orderItem.getOrderId());
                
            } else {
                // 새로운 Payment 생성
                Payments newPayment = Payments.builder()
                        .orderId(orderItem.getOrderId())
                        .userCode(orderItem.getUserCode())
                        .paymentMethod("계좌이체")
                        .paymentAmount(orderItem.getFinalPrice())
                        .paymentStatus("결제완료")
                        .paymentKey(null)
                        .pgProvider(null)
                        .build();
                
                paymentsRepository.save(newPayment);
                
                log.debug("새로운 Payment 생성 완료 - orderId: {}", orderItem.getOrderId());
            }
            
        } catch (Exception e) {
            log.error("무통장입금 Payment 처리 실패 - orderId: {}", orderItem.getOrderId(), e);
            throw new RuntimeException("Payment 처리 실패", e);
        }
    }
}