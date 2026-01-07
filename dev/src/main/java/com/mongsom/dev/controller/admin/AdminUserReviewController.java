package com.mongsom.dev.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.review.reqDto.ReviewAnswerCreateReqDto;
import com.mongsom.dev.service.admin.AdminUserReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
@Slf4j
public class AdminUserReviewController {

    private final AdminUserReviewService adminUserReviewService;
    
    /**
     * 관리자 답변 작성
     */
    @PutMapping("/answer/write")
    public ResponseEntity<RespDto<String>> writeAnswer(
            @Valid @RequestBody ReviewAnswerCreateReqDto reqDto) {
        
        log.info("=== 관리자 답변 작성 요청 ===");
        log.info("reviewId: {}", reqDto.getReviewId());
        
        RespDto<String> response = adminUserReviewService.writeAnswer(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : 
                           response.getCode() == -2 ? HttpStatus.NOT_FOUND :
                           HttpStatus.BAD_REQUEST;
        
        log.info("관리자 답변 작성 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
}
