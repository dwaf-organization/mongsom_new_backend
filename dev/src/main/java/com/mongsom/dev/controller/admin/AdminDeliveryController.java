package com.mongsom.dev.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.admin.delivery.reqDto.AdminDeliveryUpdateReqDto;
import com.mongsom.dev.dto.admin.delivery.respDto.AdminDeliveryUpdateRespDto;
import com.mongsom.dev.service.admin.AdminDeliveryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/order/delivery")
@RequiredArgsConstructor
public class AdminDeliveryController {
    
    private final AdminDeliveryService adminDeliveryService;
    
    /**
     * 배송정보 일괄 업데이트
     */
    @PutMapping("/update")
    public ResponseEntity<RespDto<AdminDeliveryUpdateRespDto>> updateDeliveryInfoBatch(
            @Valid @RequestBody AdminDeliveryUpdateReqDto reqDto) {
        
        log.info("=== 배송정보 일괄 업데이트 요청 ===");
        log.info("요청 건수: {}", reqDto.getDeliveryUpdates().size());
        
        // 요청 건수 제한 (선택사항)
        if (reqDto.getDeliveryUpdates().size() > 100) {
            log.warn("요청 건수 초과 - 요청: {}건, 최대: 100건", reqDto.getDeliveryUpdates().size());
            return ResponseEntity.badRequest().body(
                    RespDto.<AdminDeliveryUpdateRespDto>builder()
                            .code(-1)
                            .data(null)
                            .build()
            );
        }
        
        RespDto<AdminDeliveryUpdateRespDto> response = adminDeliveryService.updateDeliveryInfoBatch(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("배송정보 일괄 업데이트 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
}