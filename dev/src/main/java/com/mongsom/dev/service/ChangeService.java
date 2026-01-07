package com.mongsom.dev.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.change.reqDto.ChangeCreateReqDto;
import com.mongsom.dev.dto.change.reqDto.ChangeDeleteReqDto;
import com.mongsom.dev.entity.ChangeItem;
import com.mongsom.dev.entity.OrderDetail;
import com.mongsom.dev.entity.OrderItem;
import com.mongsom.dev.repository.ChangeItemRepository;
import com.mongsom.dev.repository.OrderDetailRepository;
import com.mongsom.dev.repository.OrderItemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeService {
    
    private final ChangeItemRepository changeItemRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderItemRepository orderItemRepository;
    
    /**
     * 교환/반품 신청
     */
    @Transactional
    public RespDto<String> createChangeRequest(ChangeCreateReqDto reqDto) {
        try {
            log.info("교환/반품 신청 시작 - userCode: {}, orderDetailId: {}, type: {}", 
                    reqDto.getUserCode(), reqDto.getOrderDetailId(), reqDto.getChangeType());
            
            // 1. 주문 상세 조회 및 검증
            Optional<OrderDetail> orderDetailOpt = orderDetailRepository.findById(reqDto.getOrderDetailId());
            if (orderDetailOpt.isEmpty()) {
                log.warn("존재하지 않는 주문 상세 - orderDetailId: {}", reqDto.getOrderDetailId());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("존재하지 않는 주문입니다.")
                        .build();
            }
            
            OrderDetail orderDetail = orderDetailOpt.get();
            
            // 2. 주문 조회
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderDetail.getOrderId());
            if (orderItemOpt.isEmpty()) {
                log.warn("연관된 주문을 찾을 수 없음 - orderId: {}", orderDetail.getOrderId());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("연관된 주문을 찾을 수 없습니다.")
                        .build();
            }
            
            OrderItem orderItem = orderItemOpt.get();
            
            // 3. 권한 확인 (사용자 코드 일치)
            if (!orderItem.getUserCode().equals(reqDto.getUserCode())) {
                log.warn("권한 없는 교환/반품 신청 시도 - userCode: {}, orderUserCode: {}", 
                        reqDto.getUserCode(), orderItem.getUserCode());
                return RespDto.<String>builder()
                        .code(-2)
                        .data("해당 주문에 대한 권한이 없습니다.")
                        .build();
            }
            
            // 4. 배송완료 상태 확인
//            if (!"배송완료".equals(orderItem.getDeliveryStatus())) {
//                log.warn("배송완료되지 않은 주문 - deliveryStatus: {}, orderId: {}", 
//                        orderItem.getDeliveryStatus(), orderItem.getOrderId());
//                return RespDto.<String>builder()
//                        .code(-3)
//                        .data("배송완료된 상품만 교환/반품 신청이 가능합니다.")
//                        .build();
//            }
            
            // 5. 중복 신청 체크
            if (changeItemRepository.existsByOrderDetailId(reqDto.getOrderDetailId())) {
                log.warn("이미 교환/반품 신청된 상품 - orderDetailId: {}", reqDto.getOrderDetailId());
                return RespDto.<String>builder()
                        .code(-4)
                        .data("이미 교환/반품 신청된 상품입니다.")
                        .build();
            }
            
            // 6. ChangeItem 생성
            ChangeItem changeItem;
            if ("교환".equals(reqDto.getChangeType())) {
                changeItem = ChangeItem.createExchange(
                        reqDto.getOrderDetailId(),
                        orderItem.getOrderId(),
                        reqDto.getUserCode(),
                        reqDto.getReason(),
                        reqDto.getRefundBank(),
                        reqDto.getRefundAccount()
                );
            } else { // 반품
                changeItem = ChangeItem.createReturn(
                        reqDto.getOrderDetailId(),
                        orderItem.getOrderId(),
                        reqDto.getUserCode(),
                        reqDto.getReason(),
                        reqDto.getRefundBank(),
                        reqDto.getRefundAccount()
                );
            }
            
            ChangeItem savedChangeItem = changeItemRepository.save(changeItem);
            
            // 7. OrderDetail의 order_status 업데이트
            int newOrderStatus = "교환".equals(reqDto.getChangeType()) ? 2 : 3;
            orderDetail.setOrderStatus(newOrderStatus);
            orderDetailRepository.save(orderDetail);
            
            log.info("교환/반품 신청 완료 - changeId: {}, orderDetailId: {}, type: {}, status: {}", 
                    savedChangeItem.getChangeId(), reqDto.getOrderDetailId(), 
                    reqDto.getChangeType(), newOrderStatus);
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("교환/반품 신청이 완료되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("교환/반품 신청 실패 - userCode: {}, orderDetailId: {}", 
                    reqDto.getUserCode(), reqDto.getOrderDetailId(), e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("교환/반품 신청 중 오류가 발생했습니다.")
                    .build();
        }
    }
    
    /**
     * 교환/반품 신청 취소
     */
    @Transactional
    public RespDto<String> deleteChangeRequest(ChangeDeleteReqDto reqDto) {
        try {
            log.info("교환/반품 취소 시작 - userCode: {}, orderDetailId: {}", 
                    reqDto.getUserCode(), reqDto.getOrderDetailId());
            
            // 1. 교환/반품 신청 조회
            Optional<ChangeItem> changeItemOpt = changeItemRepository
                    .findByUserCodeAndOrderDetailId(reqDto.getUserCode(), reqDto.getOrderDetailId());
            
            if (changeItemOpt.isEmpty()) {
                log.warn("교환/반품 신청을 찾을 수 없음 - userCode: {}, orderDetailId: {}", 
                        reqDto.getUserCode(), reqDto.getOrderDetailId());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("교환/반품 신청을 찾을 수 없습니다.")
                        .build();
            }
            
            ChangeItem changeItem = changeItemOpt.get();
            
            // 2. 신청 상태 확인 (신청 단계에서만 취소 가능)
            String changeStatus = changeItem.getChangeStatus();
            if (!"교환신청".equals(changeStatus) && !"반품신청".equals(changeStatus)) {
                log.warn("취소 불가능한 상태 - changeStatus: {}, changeId: {}", 
                        changeStatus, changeItem.getChangeId());
                return RespDto.<String>builder()
                        .code(-2)
                        .data("이미 처리 중인 신청은 취소할 수 없습니다.")
                        .build();
            }
            
            // 3. OrderDetail의 order_status를 0으로 복원
            Optional<OrderDetail> orderDetailOpt = orderDetailRepository.findById(reqDto.getOrderDetailId());
            if (orderDetailOpt.isPresent()) {
                OrderDetail orderDetail = orderDetailOpt.get();
                orderDetail.setOrderStatus(0); // 정상 상태로 복원
                orderDetailRepository.save(orderDetail);
                
                log.info("OrderDetail 상태 복원 완료 - orderDetailId: {}, orderStatus: 0", 
                        reqDto.getOrderDetailId());
            }
            
            // 4. ChangeItem 삭제
            changeItemRepository.delete(changeItem);
            
            log.info("교환/반품 취소 완료 - changeId: {}, orderDetailId: {}", 
                    changeItem.getChangeId(), reqDto.getOrderDetailId());
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("교환/반품 신청이 취소되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("교환/반품 취소 실패 - userCode: {}, orderDetailId: {}", 
                    reqDto.getUserCode(), reqDto.getOrderDetailId(), e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("교환/반품 취소 중 오류가 발생했습니다.")
                    .build();
        }
    }
}