package com.mongsom.dev.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.export.OrderExportDto;
import com.mongsom.dev.service.OrderExportService;
import com.mongsom.dev.service.ExcelExportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/export")
@RequiredArgsConstructor
public class ExportController {
    
    private final OrderExportService orderExportService;
    private final ExcelExportService excelExportService;
    
    /**
     * 주문 데이터 엑셀 내보내기
     */
    @GetMapping("/orders/excel")
    public ResponseEntity<byte[]> exportOrdersToExcel(
            @RequestParam("deliveryStatus") String deliveryStatus) {
        
        try {
            log.info("=== 주문 엑셀 내보내기 요청 ===");
            log.info("배송상태: {}", deliveryStatus);
            
            // 1. 배송상태 유효성 검증
            if (deliveryStatus == null || deliveryStatus.trim().isEmpty()) {
                log.warn("배송상태 파라미터가 없음");
                return ResponseEntity.badRequest().build();
            }
            
            // 2. 주문 데이터 조회
            List<OrderExportDto> orders = orderExportService.getOrdersForExport(deliveryStatus);
            
            if (orders.isEmpty()) {
                log.warn("조회된 주문 데이터가 없음 - deliveryStatus: {}", deliveryStatus);
                // 빈 엑셀 파일이라도 생성하여 반환 (헤더만 있는 파일)
            }
            
            // 3. 엑셀 파일 생성
            byte[] excelData = excelExportService.generateOrderExcel(orders, deliveryStatus);
            
            // 4. 파일명 생성
            String fileName = excelExportService.generateFileName(deliveryStatus);
            
            // 5. 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.setContentLength(excelData.length);
            
            log.info("=== 엑셀 내보내기 완료 ===");
            log.info("파일명: {}, 데이터 건수: {}, 파일 크기: {} bytes", 
                    fileName, orders.size(), excelData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
            
        } catch (Exception e) {
            log.error("엑셀 내보내기 실패 - deliveryStatus: {}", deliveryStatus, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
//    /**
//     * 엑셀 내보내기 미리보기 (데이터 건수 확인)
//     */
//    @GetMapping("/orders/preview")
//    public ResponseEntity<RespDto<Map<String, Object>>> previewExportData(
//            @RequestParam("deliveryStatus") String deliveryStatus) {
//        
//        try {
//            log.info("엑셀 내보내기 미리보기 요청 - deliveryStatus: {}", deliveryStatus);
//            
//            // 1. 배송상태 유효성 검증
//            if (deliveryStatus == null || deliveryStatus.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(
//                        RespDto.<Map<String, Object>>builder()
//                                .code(-1)
//                                .data(null)
//                                .build()
//                );
//            }
//            
//            // 2. 해당 배송상태의 주문 건수 조회
//            long orderCount = orderExportService.getOrderCountByDeliveryStatus(deliveryStatus);
//            
//            // 3. 미리보기 정보 생성
//            Map<String, Object> previewData = new HashMap<>();
//            previewData.put("deliveryStatus", deliveryStatus);
//            previewData.put("orderCount", orderCount);
//            previewData.put("fileName", excelExportService.generateFileName(deliveryStatus));
//            previewData.put("canExport", orderCount > 0);
//            
//            String message = orderCount > 0 
//                    ? orderCount + "건의 주문 데이터를 내보낼 수 있습니다."
//                    : "해당 배송상태에 주문 데이터가 없습니다.";
//            
//            log.info("미리보기 완료 - deliveryStatus: {}, 주문 건수: {}", deliveryStatus, orderCount);
//            
//            return ResponseEntity.ok(
//                    RespDto.<Map<String, Object>>builder()
//                            .code(1)
//                            .data(previewData)
//                            .build()
//            );
//            
//        } catch (Exception e) {
//            log.error("미리보기 실패 - deliveryStatus: {}", deliveryStatus, e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    RespDto.<Map<String, Object>>builder()
//                            .code(-1)
//                            .data(null)
//                            .build()
//            );
//        }
//    }
    
//    /**
//     * 사용 가능한 배송상태 목록 조회
//     */
//    @GetMapping("/delivery-statuses")
//    public ResponseEntity<RespDto<List<String>>> getAvailableDeliveryStatuses() {
//        
//        try {
//            log.info("사용 가능한 배송상태 목록 조회");
//            
//            List<String> statuses = orderExportService.getAvailableDeliveryStatuses();
//            
//            return ResponseEntity.ok(
//                    RespDto.<List<String>>builder()
//                            .code(1)
//                            .data(statuses)
//                            .build()
//            );
//            
//        } catch (Exception e) {
//            log.error("배송상태 목록 조회 실패", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    RespDto.<List<String>>builder()
//                            .code(-1)
//                            .data(null)
//                            .build()
//            );
//        }
//    }
//    
//    /**
//     * 전체 배송상태별 주문 건수 조회 (관리용)
//     */
//    @GetMapping("/orders/counts")
//    public ResponseEntity<RespDto<Map<String, Long>>> getOrderCountsByDeliveryStatus() {
//        
//        try {
//            log.info("전체 배송상태별 주문 건수 조회");
//            
//            List<String> statuses = orderExportService.getAvailableDeliveryStatuses();
//            Map<String, Long> counts = new HashMap<>();
//            
//            for (String status : statuses) {
//                long count = orderExportService.getOrderCountByDeliveryStatus(status);
//                counts.put(status, count);
//            }
//            
//            log.info("배송상태별 주문 건수: {}", counts);
//            
//            return ResponseEntity.ok(
//                    RespDto.<Map<String, Long>>builder()
//                            .code(1)
//                            .data(counts)
//                            .build()
//            );
//            
//        } catch (Exception e) {
//            log.error("배송상태별 주문 건수 조회 실패", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    RespDto.<Map<String, Long>>builder()
//                            .code(-1)
//                            .data(null)
//                            .build()
//            );
//        }
//    }
}