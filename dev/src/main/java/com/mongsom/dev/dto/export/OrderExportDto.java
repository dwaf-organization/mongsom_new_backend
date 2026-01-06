package com.mongsom.dev.dto.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderExportDto {
    
    // 주문 기본 정보
    private String orderNum;           // 주문번호 (order_item.order_num)
    private String receiverName;       // 수하인명 (order_item.received_user_name)
    private String receiverPhone;      // 수하인휴대폰 (order_item.received_user_phone)
    private String receiverTel;        // 수하인전화 (order_item.received_user_phone - 동일값)
    private String receiverAddress;    // 수하인주소 (address + address2)
    private String deliveryMessage;    // 배송메세지 (order_item.message)
    
    // 상품 정보
    private String productName;        // 물품명 (product.name)
    private String productOption;      // 물품옵션 (option1 + option2 이름)
    private Integer quantity;          // 내품수량 (order_detail.quantity)
    
    // 배송 정보 (고정값들)
    private String shippingType;       // 운임구분 ("010" 고정)
    private Integer packageCount;      // 택배수량 (1 고정)
    private Integer shippingCost;      // 택배운임 (2750 고정)
    
    // 내부적으로 사용할 정보들
    private Integer orderId;           // 주문 ID (내부용)
    private Integer productId;         // 상품 ID (내부용)
    private Integer option1;           // 옵션1 ID (내부용)
    private Integer option2;           // 옵션2 ID (내부용)
}