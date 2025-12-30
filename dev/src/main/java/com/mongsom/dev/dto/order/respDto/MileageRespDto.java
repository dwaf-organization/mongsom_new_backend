package com.mongsom.dev.dto.order.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MileageRespDto {
    
    private Long userCode;
    private Integer mileage;
    private String userName; // 사용자명 (선택적)
    
    public static MileageRespDto from(Long userCode, Integer mileage, String userName) {
        return MileageRespDto.builder()
                .userCode(userCode)
                .mileage(mileage)
                .userName(userName)
                .build();
    }
    
    public static MileageRespDto from(Long userCode, Integer mileage) {
        return MileageRespDto.builder()
                .userCode(userCode)
                .mileage(mileage)
                .build();
    }
}