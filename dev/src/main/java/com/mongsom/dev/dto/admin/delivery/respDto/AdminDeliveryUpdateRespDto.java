package com.mongsom.dev.dto.admin.delivery.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDeliveryUpdateRespDto {
    
    private BatchResultDto batchResult;
    private List<UpdateResultDto> results;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchResultDto {
        private Integer totalCount;      // 전체 요청 개수
        private Integer successCount;    // 성공 개수
        private Integer failureCount;    // 실패 개수
        private String summary;          // 요약 메시지
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateResultDto {
        private Integer orderId;         // 주문 ID
        private Boolean success;         // 성공 여부
        private String message;          // 성공/실패 메시지
        private Boolean bankTransferProcessed; // 무통장입금 처리 여부
    }
}