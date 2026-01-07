package com.mongsom.dev.dto.admin.change.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChangeStatusUpdateReqDto {
    
    @NotNull(message = "교환/반품 ID는 필수입니다.")
    private Integer changeId;
    
    @NotBlank(message = "변경할 상태는 필수입니다.")
    @Pattern(regexp = "^(반품신청|교환신청|교환승인|반품승인|교환반려|반품반려|반품중|교환중|반품완료|교환완료)$", 
             message = "상태는 '반품신청', '교환신청', '교환승인', '반품승인', '교환반려', '반품반려','반품중', '교환중', '반품완료', '교환완료' 중 하나여야 합니다.")
    private String newStatus;
}