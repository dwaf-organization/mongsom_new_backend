package com.mongsom.dev.controller.admin;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.admin.change.reqDto.AdminChangeStatusUpdateReqDto;
import com.mongsom.dev.dto.admin.change.respDto.AdminChangeDetailRespDto;
import com.mongsom.dev.dto.admin.change.respDto.AdminChangeListRespDto;
import com.mongsom.dev.dto.admin.change.respDto.AdminChangeStatusUpdateRespDto;
import com.mongsom.dev.service.admin.AdminChangeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/product/change")
@RequiredArgsConstructor
public class AdminChangeController {
    
    private final AdminChangeService adminChangeService;
    
    /**
     * 관리자 교환/반품 목록 조회
     */
    @GetMapping("/list/{changeStatus}/{page}/{size}")
    public ResponseEntity<RespDto<AdminChangeListRespDto>> getAdminChangeList(
            @PathVariable("changeStatus") Integer changeStatus,
            @PathVariable("page") Integer page,
            @PathVariable("size") Integer size) {
        
        log.info("=== 관리자 교환/반품 조회 요청 ===");
        log.info("changeStatus: {}, page: {}, size: {}", changeStatus, page, size);
        
        // 페이징 유효성 검증
        if (page < 0) {
            log.warn("잘못된 page 값 - page: {}", page);
            return ResponseEntity.badRequest().body(
                    RespDto.<AdminChangeListRespDto>builder()
                            .code(-1)
                            .data(null)
                            .build()
            );
        }
        
        if (size < 1 || size > 100) {
            log.warn("잘못된 size 값 - size: {}", size);
            return ResponseEntity.badRequest().body(
                    RespDto.<AdminChangeListRespDto>builder()
                            .code(-1)
                            .data(null)
                            .build()
            );
        }
        
        // changeStatus 유효성 검증
        if (changeStatus == null || (changeStatus != 1 && changeStatus != 2)) {
            log.warn("잘못된 changeStatus 값 - changeStatus: {}", changeStatus);
            return ResponseEntity.badRequest().body(
                    RespDto.<AdminChangeListRespDto>builder()
                            .code(-1)
                            .data(null)
                            .build()
            );
        }
        
        Pageable pageable = PageRequest.of(page, size);
        RespDto<AdminChangeListRespDto> response = adminChangeService.getAdminChangeList(changeStatus, pageable);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("관리자 교환/반품 조회 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 관리자 교환/반품 상세 조회
     */
    @GetMapping("/detail/{changeId}")
    public ResponseEntity<RespDto<AdminChangeDetailRespDto>> getAdminChangeDetail(
            @PathVariable("changeId") Integer changeId) {
        
        log.info("=== 관리자 교환/반품 상세조회 요청 ===");
        log.info("changeId: {}", changeId);
        
        RespDto<AdminChangeDetailRespDto> response = adminChangeService.getAdminChangeDetail(changeId);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("관리자 교환/반품 상세조회 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 관리자 교환/반품 상태 변경
     */
    @PutMapping("/status")
    public ResponseEntity<RespDto<AdminChangeStatusUpdateRespDto>> updateChangeStatus(
            @Valid @RequestBody AdminChangeStatusUpdateReqDto reqDto) {
        
        log.info("=== 교환/반품 상태 변경 요청 ===");
        log.info("changeId: {}, newStatus: {}", reqDto.getChangeId(), reqDto.getNewStatus());
        
        RespDto<AdminChangeStatusUpdateRespDto> response = adminChangeService.updateChangeStatus(reqDto);
        
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.BAD_REQUEST :
                           HttpStatus.INTERNAL_SERVER_ERROR;
        
        log.info("교환/반품 상태 변경 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
}
