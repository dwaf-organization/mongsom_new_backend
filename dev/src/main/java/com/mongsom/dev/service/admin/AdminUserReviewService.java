package com.mongsom.dev.service.admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.review.reqDto.ReviewAnswerCreateReqDto;
import com.mongsom.dev.entity.UserReview;
import com.mongsom.dev.repository.UserReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserReviewService {

    private final UserReviewRepository userReviewRepository;
    
    /**
     * 관리자 답변 작성
     */
    @Transactional
    public RespDto<String> writeAnswer(ReviewAnswerCreateReqDto reqDto) {
        try {
            log.info("관리자 답변 작성 시작 - reviewId: {}", reqDto.getReviewId());
            
            // 리뷰 존재 여부 확인
            Optional<UserReview> reviewOpt = userReviewRepository.findById(reqDto.getReviewId());
            if (reviewOpt.isEmpty()) {
                log.warn("존재하지 않는 리뷰 - reviewId: {}", reqDto.getReviewId());
                return RespDto.<String>builder()
                        .code(-2)
                        .data("답변등록을 실패했습니다.")
                        .build();
            }
            
            UserReview review = reviewOpt.get();
            
            // 관리자 답변 및 답변일자 설정
            review.setAdminAnswer(reqDto.getAdminAnswer());
            review.setAdminAnswerAt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            
            // 저장
            userReviewRepository.save(review);
            
            log.info("관리자 답변 작성 완료 - reviewId: {}", reqDto.getReviewId());
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("답변을 등록했습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("관리자 답변 작성 실패 - reviewId: {}", reqDto.getReviewId(), e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("답변등록을 실패했습니다.")
                    .build();
        }
    }
}
