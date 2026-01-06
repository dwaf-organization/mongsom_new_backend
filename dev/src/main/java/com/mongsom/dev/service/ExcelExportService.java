package com.mongsom.dev.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.mongsom.dev.dto.export.OrderExportDto;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class ExcelExportService {
    
    // 엑셀 헤더 정의 (로젠택배 양식)
    private static final String[] EXCEL_HEADERS = {
        "주문번호", "수하인명", "수하인휴대폰", "수하인전화", 
        "수하인주소", "물품명", "물품옵션", "내품수량", 
        "배송메세지", "운임구분", "택배수량", "택배운임"
    };
    
    /**
     * 주문 데이터를 엑셀 파일로 변환
     */
    public byte[] generateOrderExcel(List<OrderExportDto> orders, String deliveryStatus) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            log.info("엑셀 파일 생성 시작 - 데이터 건수: {}, 배송상태: {}", orders.size(), deliveryStatus);
            
            // 1. 워크시트 생성 (원본 파일과 동일한 시트명)
            Sheet sheet = workbook.createSheet("엑셀파일첫행-제목있음(주소1,2로분리)");
            
            // 2. 스타일 설정
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // 3. 헤더 행 생성 (1행)
            createHeaderRow(sheet, headerStyle);
            
            // 4. 데이터 행 생성 (2행부터)
            createDataRows(sheet, orders, dataStyle);
            
            // 5. 컬럼 너비 자동 조정
            autoSizeColumns(sheet);
            
            // 6. 엑셀 파일을 바이트 배열로 변환
            workbook.write(out);
            
            log.info("엑셀 파일 생성 완료 - 총 {}행 (헤더 1행 + 데이터 {}행)", 
                    orders.size() + 1, orders.size());
            
            return out.toByteArray();
            
        } catch (IOException e) {
            log.error("엑셀 파일 생성 실패 - 데이터 건수: {}", orders.size(), e);
            throw new RuntimeException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 헤더 스타일 생성
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 배경색 (연한 회색)
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 폰트 설정 (굵게)
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName("맑은 고딕");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        
        // 테두리
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 가운데 정렬
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    /**
     * 데이터 스타일 생성
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 폰트 설정
        Font font = workbook.createFont();
        font.setFontName("맑은 고딕");
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        
        // 테두리
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 세로 가운데 정렬
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    /**
     * 헤더 행 생성 (1행)
     */
    private void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);  // 1행 (0-based index)
        
        for (int i = 0; i < EXCEL_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(EXCEL_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
        
        log.debug("헤더 행 생성 완료 - 컬럼 수: {}", EXCEL_HEADERS.length);
    }
    
    /**
     * 데이터 행들 생성 (2행부터)
     */
    private void createDataRows(Sheet sheet, List<OrderExportDto> orders, CellStyle dataStyle) {
        for (int i = 0; i < orders.size(); i++) {
            Row row = sheet.createRow(i + 1);  // 2행부터 시작 (i+1)
            OrderExportDto order = orders.get(i);
            
            createDataRow(row, order, dataStyle);
        }
        
        log.debug("데이터 행 생성 완료 - 행 수: {}", orders.size());
    }
    
    /**
     * 개별 데이터 행 생성
     */
    private void createDataRow(Row row, OrderExportDto order, CellStyle dataStyle) {
        // 각 컬럼별 데이터 설정
        createCell(row, 0, order.getOrderNum(), dataStyle);                    // 주문번호
        createCell(row, 1, order.getReceiverName(), dataStyle);                // 수하인명
        createCell(row, 2, order.getReceiverPhone(), dataStyle);               // 수하인휴대폰
        createCell(row, 3, order.getReceiverTel(), dataStyle);                 // 수하인전화
        createCell(row, 4, order.getReceiverAddress(), dataStyle);             // 수하인주소
        createCell(row, 5, order.getProductName(), dataStyle);                 // 물품명
        createCell(row, 6, order.getProductOption(), dataStyle);               // 물품옵션
        createCell(row, 7, order.getQuantity(), dataStyle);                    // 내품수량
        createCell(row, 8, order.getDeliveryMessage(), dataStyle);             // 배송메세지
        createCell(row, 9, order.getShippingType(), dataStyle);                // 운임구분
        createCell(row, 10, order.getPackageCount(), dataStyle);               // 택배수량
        createCell(row, 11, order.getShippingCost(), dataStyle);               // 택배운임
    }
    
    /**
     * 셀 생성 및 값 설정 (문자열용)
     */
    private void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
    
    /**
     * 셀 생성 및 값 설정 (숫자용)
     */
    private void createCell(Row row, int columnIndex, Integer value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }
    
    /**
     * 컬럼 너비 자동 조정
     */
    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < EXCEL_HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
            
            // 최대/최소 너비 제한
            int currentWidth = sheet.getColumnWidth(i);
            if (currentWidth > 15000) {  // 최대 너비 제한
                sheet.setColumnWidth(i, 15000);
            } else if (currentWidth < 2000) {  // 최소 너비 제한
                sheet.setColumnWidth(i, 2000);
            }
        }
        
        log.debug("컬럼 너비 자동 조정 완료");
    }
    
    /**
     * 엑셀 파일명 생성 (로젠택배(몽솜)_20260106.xlsx)
     */
    public String generateFileName() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "로젠택배(몽솜)_" + today + ".xlsx";
    }
    
    /**
     * 엑셀 파일명 생성 (배송상태 포함)
     */
    public String generateFileName(String deliveryStatus) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "로젠택배(몽솜)_" + deliveryStatus + "_" + today + ".xlsx";
    }
}