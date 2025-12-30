package com.mongsom.dev.dto.admin.user.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MileageChargeRespDto {
    
    private Long userCode;
    private String userName;
    private Integer beforeMileage;
    private Integer afterMileage;
    private Integer chargedAmount;
    private String message;
    
    public static MileageChargeRespDto success(Long userCode, String userName, 
                                             Integer beforeMileage, Integer afterMileage, 
                                             Integer chargedAmount) {
        return MileageChargeRespDto.builder()
                .userCode(userCode)
                .userName(userName)
                .beforeMileage(beforeMileage)
                .afterMileage(afterMileage)
                .chargedAmount(chargedAmount)
                .message("마일리지가 성공적으로 충전되었습니다.")
                .build();
    }
    
    public static MileageChargeRespDto failure(String message) {
        return MileageChargeRespDto.builder()
                .message(message)
                .build();
    }
}