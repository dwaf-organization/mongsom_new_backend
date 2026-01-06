package com.mongsom.dev.dto.admin.change.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChangeListRespDto {
    
    private List<AdminChangeItemDto> changes;
    private PaginationDto pagination;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminChangeItemDto {
        private LocalDateTime requestedAt;       // change_item.requested_at (주문일)
        private String orderNum;                 // order_item.order_num (주문번호)
        private String receivedUserName;         // order_item.received_user_name (받는사람)
        
        // 상품정보
        private ProductInfoDto productInfo;
        
        private Integer finalPrice;              // order_item.final_price (구매금액)
        private String changeStatus;             // change_item.change_status (상태)
        
        // 추가 정보
        private Integer changeId;                // change_item.change_id
        private Integer orderId;                 // change_item.order_id
        private Integer orderDetailId;           // change_item.order_detail_id
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfoDto {
        private String productName;              // product.name (상품명)
        private String productImgUrl;            // product_images[0] (대표 이미지)
        
        // 옵션 정보 (개별)
        private Integer option1;                 // order_detail.option1
        private Integer option2;                 // order_detail.option2
        private String option1Name;              // product_option_value.value_name
        private String option2Name;              // product_option_value.value_name
        
        // 옵션 조합
        private String optionComb;               // "500ml, 블랙" (조합된 문자열)
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationDto {
        private Integer currentPage;
        private Integer totalPage;
        private Integer size;
        private Long totalElements;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }
}