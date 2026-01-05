package com.mongsom.dev.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.qna.reqDto.QnaAnswerReqDto;
import com.mongsom.dev.dto.qna.reqDto.QnaCreateReqDto;
import com.mongsom.dev.dto.qna.reqDto.QnaUpdateReqDto;
import com.mongsom.dev.dto.qna.respDto.ProductQnaListRespDto;
import com.mongsom.dev.dto.qna.respDto.QnaDetailRespDto;
import com.mongsom.dev.dto.qna.respDto.QnaListRespDto;
import com.mongsom.dev.service.QnaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/qna")
@RequiredArgsConstructor
public class QnaController {
    
    private final QnaService qnaService;
    
    /**
     * QNA 목록 조회 (페이징)
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<QnaListRespDto>> getQnaList(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        
        log.info("=== QNA 목록 조회 요청 ===");
        log.info("page: {}, size: {}", page, size);
        
        // 페이지 유효성 검증
        if (page < 0) {
            log.warn("잘못된 페이지 번호: {}", page);
            return ResponseEntity.badRequest().body(
                RespDto.<QnaListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        if (size < 1 || size > 100) {
            log.warn("잘못된 페이지 크기: {}", size);
            return ResponseEntity.badRequest().body(
                RespDto.<QnaListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        Pageable pageable = PageRequest.of(page, size);
        RespDto<QnaListRespDto> response = qnaService.getQnaList(pageable);
        
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        
        log.info("QNA 목록 조회 완료 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * QNA 상세 조회
     */
    @GetMapping("/detail")
    public ResponseEntity<RespDto<QnaDetailRespDto>> getQnaDetail(
            @RequestParam("qnaCode") Integer qnaCode) {
        
        log.info("=== QNA 상세조회 요청 ===");
        log.info("qnaCode: {}", qnaCode);
        
        // qnaCode 유효성 검증
        if (qnaCode == null || qnaCode <= 0) {
            log.warn("잘못된 QNA 코드: {}", qnaCode);
            return ResponseEntity.badRequest().body(
                RespDto.<QnaDetailRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        RespDto<QnaDetailRespDto> response = qnaService.getQnaDetail(qnaCode);
        
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.NOT_FOUND : 
                           HttpStatus.INTERNAL_SERVER_ERROR;
        
        log.info("QNA 상세조회 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 상품별 QNA 목록 조회
     */
    @GetMapping("/product/list")
    public ResponseEntity<RespDto<ProductQnaListRespDto>> getProductQnaList(
            @RequestParam("productId") Integer productId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        
        log.info("=== 상품별 QNA 목록 조회 요청 ===");
        log.info("productId: {}, page: {}, size: {}", productId, page, size);
        
        // 파라미터 유효성 검증
        if (productId == null || productId <= 0) {
            log.warn("잘못된 상품 ID: {}", productId);
            return ResponseEntity.badRequest().body(
                RespDto.<ProductQnaListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        if (page < 0) {
            return ResponseEntity.badRequest().body(
                RespDto.<ProductQnaListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        if (size < 1 || size > 100) {
            return ResponseEntity.badRequest().body(
                RespDto.<ProductQnaListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build()
            );
        }
        
        Pageable pageable = PageRequest.of(page, size);
        RespDto<ProductQnaListRespDto> response = qnaService.getProductQnaList(productId, pageable);
        
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        
        log.info("상품별 QNA 목록 조회 완료 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * QNA 생성
     */
    @PostMapping("/create")
    public ResponseEntity<RespDto<String>> createQna(
            @Valid @RequestBody QnaCreateReqDto reqDto) {
        
        log.info("=== QNA 생성 요청 ===");
        log.info("userCode: {}, productId: {}, title: {}", 
                reqDto.getUserCode(), reqDto.getProductId(), reqDto.getQnaTitle());
        
        RespDto<String> response = qnaService.createQna(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        
        log.info("QNA 생성 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * QNA 수정 (내용만)
     */
    @PutMapping("/update")
    public ResponseEntity<RespDto<String>> updateQna(
            @Valid @RequestBody QnaUpdateReqDto reqDto) {
        
        log.info("=== QNA 수정 요청 ===");
        log.info("qnaCode: {}, userCode: {}", reqDto.getQnaCode(), reqDto.getUserCode());
        
        RespDto<String> response = qnaService.updateQna(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.NOT_FOUND : 
                           response.getCode() == -3 ? HttpStatus.FORBIDDEN :
                           HttpStatus.BAD_REQUEST;
        
        log.info("QNA 수정 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * QNA 삭제 (작성자만)
     */
    @DeleteMapping("/delete/{userCode}/{qnaCode}")
    public ResponseEntity<RespDto<String>> deleteQna(
            @PathVariable("userCode") Long userCode,
            @PathVariable("qnaCode") Integer qnaCode) {
        
        log.info("=== QNA 삭제 요청 ===");
        log.info("userCode: {}, qnaCode: {}", userCode, qnaCode);
        
        // 파라미터 유효성 검증
        if (userCode == null || userCode <= 0) {
            log.warn("잘못된 사용자 코드: {}", userCode);
            return ResponseEntity.badRequest().body(
                RespDto.<String>builder()
                    .code(-1)
                    .data("유효하지 않은 사용자 코드입니다.")
                    .build()
            );
        }
        
        if (qnaCode == null || qnaCode <= 0) {
            log.warn("잘못된 QNA 코드: {}", qnaCode);
            return ResponseEntity.badRequest().body(
                RespDto.<String>builder()
                    .code(-1)
                    .data("유효하지 않은 QNA 코드입니다.")
                    .build()
            );
        }
        
        RespDto<String> response = qnaService.deleteQna(userCode, qnaCode);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.NOT_FOUND : 
                           response.getCode() == -3 ? HttpStatus.FORBIDDEN : 
                           HttpStatus.INTERNAL_SERVER_ERROR;
        
        log.info("QNA 삭제 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * QNA 답변 등록 (관리자용)
     */
    @PutMapping("/answer")
    public ResponseEntity<RespDto<String>> answerQna(
            @Valid @RequestBody QnaAnswerReqDto reqDto) {
        
        log.info("=== QNA 답변 등록 요청 ===");
        log.info("qnaCode: {}", reqDto.getQnaCode());
        
        RespDto<String> response = qnaService.answerQna(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.NOT_FOUND : 
                           HttpStatus.BAD_REQUEST;
        
        log.info("QNA 답변 등록 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
}