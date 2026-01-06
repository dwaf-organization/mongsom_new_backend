package com.mongsom.dev.dto.admin.change.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChangeStatusUpdateRespDto {
    
    private Integer changeId;                // 교환/반품 ID
    private String previousStatus;           // 이전 상태
    private String newStatus;                // 변경된 상태
    private LocalDateTime processedAt;       // 처리일시
    private String message;                  // 성공/실패 메시지
}