package com.mongsom.dev.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongsom.dev.dto.export.OrderExportDto;
import com.mongsom.dev.entity.OrderItem;
import com.mongsom.dev.entity.OrderDetail;
import com.mongsom.dev.entity.Product;
import com.mongsom.dev.repository.OrderItemRepository;
import com.mongsom.dev.repository.OrderDetailRepository;
import com.mongsom.dev.repository.ProductRepository;
import com.mongsom.dev.repository.ProductOptionValueRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderExportService {
    
    private final OrderItemRepository orderItemRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    
    /**
     * 배송 상태별 주문 데이터 조회 (엑셀 내보내기용)
     */
    @Transactional(readOnly = true)
    public List<OrderExportDto> getOrdersForExport(String deliveryStatus) {
        try {
            log.info("엑셀 내보내기용 주문 조회 시작 - deliveryStatus: {}", deliveryStatus);
            
            // 1. 배송 상태별 주문 목록 조회 (결제일시 내림차순)
            List<OrderItem> orderItems = orderItemRepository.findByDeliveryStatusOrderByPaymentAtDesc(deliveryStatus);
            
            log.info("조회된 주문 수: {}", orderItems.size());
            
            // 2. 각 주문의 상품별로 행 생성
            List<OrderExportDto> exportData = new ArrayList<>();
            
            for (OrderItem orderItem : orderItems) {
                List<OrderExportDto> orderRows = processOrderItem(orderItem);
                exportData.addAll(orderRows);
            }
            
            log.info("엑셀 내보내기용 데이터 생성 완료 - 총 행 수: {}", exportData.size());
            
            return exportData;
            
        } catch (Exception e) {
            log.error("엑셀 내보내기용 데이터 조회 실패 - deliveryStatus: {}", deliveryStatus, e);
            throw new RuntimeException("엑셀 데이터 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 하나의 주문에서 각 상품별로 OrderExportDto 생성
     */
    private List<OrderExportDto> processOrderItem(OrderItem orderItem) {
        try {
            // 1. 해당 주문의 상품 목록 조회
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderItem.getOrderId());
            
            if (orderDetails.isEmpty()) {
                log.warn("주문 상세를 찾을 수 없음 - orderId: {}", orderItem.getOrderId());
                return new ArrayList<>();
            }
            
            // 2. 각 상품별로 행 데이터 생성
            List<OrderExportDto> orderRows = new ArrayList<>();
            
            for (OrderDetail orderDetail : orderDetails) {
                OrderExportDto exportDto = createExportDto(orderItem, orderDetail);
                if (exportDto != null) {
                    orderRows.add(exportDto);
                }
            }
            
            log.debug("주문 처리 완료 - orderId: {}, 상품 수: {}", orderItem.getOrderId(), orderRows.size());
            
            return orderRows;
            
        } catch (Exception e) {
            log.error("주문 처리 실패 - orderId: {}", orderItem.getOrderId(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * OrderItem + OrderDetail로부터 OrderExportDto 생성
     */
    private OrderExportDto createExportDto(OrderItem orderItem, OrderDetail orderDetail) {
        try {
            // 1. 상품 정보 조회
            Optional<Product> productOpt = productRepository.findById(orderDetail.getProductId());
            if (productOpt.isEmpty()) {
                log.warn("상품을 찾을 수 없음 - productId: {}", orderDetail.getProductId());
                return null;
            }
            
            Product product = productOpt.get();
            
            // 2. 수하인 주소 조합 (address + address2)
            String receiverAddress = combineAddress(
                orderItem.getReceivedUserAddress(), 
                orderItem.getReceivedUserAddress2()
            );
            
            // 3. 옵션 정보 조회 및 조합
            String productOption = combineOptions(orderDetail.getOption1(), orderDetail.getOption2());
            
            // 4. OrderExportDto 생성
            return OrderExportDto.builder()
                    .orderNum(orderItem.getOrderNum())
                    .receiverName(orderItem.getReceivedUserName())
                    .receiverPhone(orderItem.getReceivedUserPhone())
                    .receiverTel(orderItem.getReceivedUserPhone())  // 동일값
                    .receiverAddress(receiverAddress)
                    .deliveryMessage(orderItem.getMessage())
                    .productName(product.getName())
                    .productOption(productOption)
                    .quantity(orderDetail.getQuantity())
                    .shippingType("010")        // 고정값
                    .packageCount(1)            // 고정값
                    .shippingCost(2750)         // 고정값
                    .orderId(orderItem.getOrderId())
                    .productId(orderDetail.getProductId())
                    .option1(orderDetail.getOption1())
                    .option2(orderDetail.getOption2())
                    .build();
            
        } catch (Exception e) {
            log.error("ExportDto 생성 실패 - orderId: {}, productId: {}", 
                    orderItem.getOrderId(), orderDetail.getProductId(), e);
            return null;
        }
    }
    
    /**
     * 주소 조합 (address + address2)
     */
    private String combineAddress(String address, String address2) {
        if (address == null) address = "";
        if (address2 == null) address2 = "";
        
        String combinedAddress = (address + " " + address2).trim();
        return combinedAddress.isEmpty() ? "주소정보없음" : combinedAddress;
    }
    
    /**
     * 옵션 정보 조합 (option1 + option2 이름)
     */
    private String combineOptions(Integer option1, Integer option2) {
        List<String> optionNames = new ArrayList<>();
        
        // option1 이름 조회
        if (option1 != null) {
            String option1Name = getOptionValueName(option1);
            if (option1Name != null && !option1Name.isEmpty()) {
                optionNames.add(option1Name);
            }
        }
        
        // option2 이름 조회
        if (option2 != null) {
            String option2Name = getOptionValueName(option2);
            if (option2Name != null && !option2Name.isEmpty()) {
                optionNames.add(option2Name);
            }
        }
        
        // 옵션명 조합
        if (optionNames.isEmpty()) {
            return "옵션없음";
        } else {
            return String.join(" ", optionNames);  // 공백으로 연결
        }
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
    
//    /**
//     * 전체 배송 상태 목록 조회 (관리용)
//     */
//    @Transactional(readOnly = true)
//    public List<String> getAvailableDeliveryStatuses() {
//        try {
//            List<String> statuses = orderItemRepository.findDistinctDeliveryStatuses();
//            log.info("사용 가능한 배송 상태: {}", statuses);
//            return statuses;
//            
//        } catch (Exception e) {
//            log.error("배송 상태 목록 조회 실패", e);
//            return new ArrayList<>();
//        }
//    }
//    
//    /**
//     * 배송 상태별 주문 건수 조회 (미리보기용)
//     */
//    @Transactional(readOnly = true)
//    public long getOrderCountByDeliveryStatus(String deliveryStatus) {
//        try {
//            return orderItemRepository.countByDeliveryStatus(deliveryStatus);
//        } catch (Exception e) {
//            log.error("주문 건수 조회 실패 - deliveryStatus: {}", deliveryStatus, e);
//            return 0;
//        }
//    }
}