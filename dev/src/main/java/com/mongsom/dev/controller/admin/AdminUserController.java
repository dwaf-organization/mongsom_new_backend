package com.mongsom.dev.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.admin.user.reqDto.AdminLoginReqDto;
import com.mongsom.dev.dto.admin.user.reqDto.MileageChargeReqDto;
import com.mongsom.dev.dto.admin.user.respDto.AdminUserListRespDto;
import com.mongsom.dev.dto.admin.user.respDto.MileageChargeRespDto;
import com.mongsom.dev.service.admin.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {
    
    private final AdminUserService adminUserService;
    
    /**
     * 관리자 회원 정보 조회 (검색 기능 포함)
     * 
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param searchItem 검색어 (선택적, 이름 또는 전화번호 부분검색)
     * @return 회원 목록 및 페이지네이션 정보
     */
    @GetMapping("/user/list/{page}/{size}")
    public ResponseEntity<RespDto<AdminUserListRespDto>> getUserList(
            @PathVariable("page") Integer page,
            @PathVariable("size") Integer size,
            @RequestParam(value = "searchItem", required = false) String searchItem) {
        
        // 검색어가 있는 경우와 없는 경우 로그 구분
        if (searchItem != null && !searchItem.trim().isEmpty()) {
            log.info("=== 관리자 회원 검색 요청 ===");
            log.info("page: {}, size: {}, searchItem: '{}'", page, size, searchItem.trim());
        } else {
            log.info("=== 관리자 회원 전체 조회 요청 ===");
            log.info("page: {}, size: {}", page, size);
        }
        
        // Service 호출
        RespDto<AdminUserListRespDto> response = adminUserService.getUserList(page, size, searchItem);
        
        // 결과 로그
        if (response.getCode() == 1 && response.getData() != null) {
            int userCount = response.getData().getUsers() != null ? response.getData().getUsers().size() : 0;
            
            if (searchItem != null && !searchItem.trim().isEmpty()) {
                log.info("회원 검색 완료 - 검색어: '{}', 조회된 회원 수: {}", searchItem.trim(), userCount);
            } else {
                log.info("회원 전체 조회 완료 - 조회된 회원 수: {}", userCount);
            }
        } else {
            log.warn("회원 조회 실패 - code: {}", response.getCode());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 관리자 로그인
     * POST /api/v1/admin/auth/login
     * 
     * @param reqDto 아이디, 비밀번호
     * @return 로그인 성공 시 관리자 정보
     */
    @PostMapping("/auth/login")
    public ResponseEntity<RespDto<Long>> adminLogin(
            @Valid @RequestBody AdminLoginReqDto reqDto) {
        
        log.info("관리자 로그인 요청 - userId: {}", reqDto.getUserId());
        
        RespDto<Long> response = adminUserService.adminLogin(reqDto);
        
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 사용자 마일리지 충전
     */
    @PostMapping("/user/mileage/charge")
    public ResponseEntity<RespDto<MileageChargeRespDto>> chargeMileage(
            @Valid @RequestBody MileageChargeReqDto reqDto) {
        
        log.info("=== 관리자 마일리지 충전 요청 ===");
        log.info("userCode: {}, chargeAmount: {}", reqDto.getUserCode(), reqDto.getChargeAmount());
        
        RespDto<MileageChargeRespDto> response = adminUserService.chargeMileage(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("마일리지 충전 결과 - code: {}", response.getCode());
        if (response.getData() != null && response.getData().getAfterMileage() != null) {
            log.info("충전 완료 - 충전 전: {}, 충전 후: {}, 충전액: {}", 
                    response.getData().getBeforeMileage(), 
                    response.getData().getAfterMileage(), 
                    response.getData().getChargedAmount());
        }
        
        return ResponseEntity.status(status).body(response);
    }
}