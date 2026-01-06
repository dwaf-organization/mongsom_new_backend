package com.mongsom.dev.dto.admin.order.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderSearchReqDto {
    
    private LocalDate startDate;         // 시작일자 (yyyy-MM-dd)
    private LocalDate endDate;           // 끝일자 (yyyy-MM-dd)
    private String searchKeyword;        // 검색 키워드 (주문번호, 송장번호, 전화번호, 이름)
    private String orderStatus;          // 주문상태 (delivery_status)
}