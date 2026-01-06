package com.mongsom.dev.controller.admin;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.admin.order.reqDto.AdminOrderSearchReqDto;
import com.mongsom.dev.dto.admin.order.respDto.AdminOrderListRespDto;
import com.mongsom.dev.service.admin.AdminOrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {
    
    private final AdminOrderService adminOrderService;
    
    /**
     * 관리자 주문 목록 조회
     */
    @GetMapping("/list/{page}/{size}")
    public ResponseEntity<RespDto<AdminOrderListRespDto>> getAdminOrderList(
            @PathVariable("page") Integer page,
            @PathVariable("size") Integer size,
            @RequestParam(value = "startDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "orderStatus", required = false) String orderStatus) {
        
        log.info("=== 관리자 주문조회 요청 ===");
        log.info("page: {}, size: {}, startDate: {}, endDate: {}, keyword: {}, status: {}", 
                page, size, startDate, endDate, searchKeyword, orderStatus);
        
        // 페이징 유효성 검증
        if (page < 0) {
            log.warn("잘못된 page 값 - page: {}", page);
            return ResponseEntity.badRequest().body(
                    RespDto.<AdminOrderListRespDto>builder()
                            .code(-1)
                            .data(null)
                            .build()
            );
        }
        
        if (size < 1 || size > 100) {
            log.warn("잘못된 size 값 - size: {}", size);
            return ResponseEntity.badRequest().body(
                    RespDto.<AdminOrderListRespDto>builder()
                            .code(-1)
                            .data(null)
                            .build()
            );
        }
        
        // 검색 조건 구성
        AdminOrderSearchReqDto searchDto = AdminOrderSearchReqDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .searchKeyword(searchKeyword)
                .orderStatus(orderStatus)
                .build();
        
        Pageable pageable = PageRequest.of(page, size);
        RespDto<AdminOrderListRespDto> response = adminOrderService.getAdminOrderList(searchDto, pageable);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("관리자 주문조회 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
}