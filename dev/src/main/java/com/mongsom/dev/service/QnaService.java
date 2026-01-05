package com.mongsom.dev.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.qna.reqDto.QnaAnswerReqDto;
import com.mongsom.dev.dto.qna.reqDto.QnaCreateReqDto;
import com.mongsom.dev.dto.qna.reqDto.QnaUpdateReqDto;
import com.mongsom.dev.dto.qna.respDto.ProductQnaListRespDto;
import com.mongsom.dev.dto.qna.respDto.QnaDetailRespDto;
import com.mongsom.dev.dto.qna.respDto.QnaListRespDto;
import com.mongsom.dev.entity.Qna;
import com.mongsom.dev.entity.User;
import com.mongsom.dev.repository.QnaRepository;
import com.mongsom.dev.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QnaService {
    
    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;
    
    /**
     * QNA 목록 조회 (페이징)
     */
    @Transactional
    public RespDto<QnaListRespDto> getQnaList(Pageable pageable) {
        try {
            log.info("QNA 목록 조회 시작 - page: {}, size: {}", 
                    pageable.getPageNumber(), pageable.getPageSize());
            
            // QNA 목록 조회 (최신순)
            Page<Qna> qnaPage = qnaRepository.findAllByOrderByCreatedAtDesc(pageable);
            
            // DTO 변환
            List<QnaListRespDto.QnaItemDto> qnaList = qnaPage.getContent()
                    .stream()
                    .map(this::convertToQnaItemDto)
                    .collect(Collectors.toList());
            
            // 페이지 정보 생성
            QnaListRespDto.PaginationInfo pagination = QnaListRespDto.PaginationInfo.builder()
                    .currentPage(qnaPage.getNumber())
                    .pageSize(qnaPage.getSize())
                    .totalPages(qnaPage.getTotalPages())
                    .totalElements(qnaPage.getTotalElements())
                    .hasNext(qnaPage.hasNext())
                    .hasPrevious(qnaPage.hasPrevious())
                    .build();
            
            // 응답 데이터 생성
            QnaListRespDto responseData = QnaListRespDto.builder()
                    .qnaList(qnaList)
                    .pagination(pagination)
                    .build();
            
            log.info("QNA 목록 조회 완료 - 총 {}건, 현재 페이지: {}/{}", 
                    qnaPage.getTotalElements(), 
                    qnaPage.getNumber() + 1, 
                    qnaPage.getTotalPages());
            
            return RespDto.<QnaListRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("QNA 목록 조회 실패", e);
            return RespDto.<QnaListRespDto>builder()
                    .code(-1)
                    .build();
        }
    }
    
    /**
     * Qna 엔티티를 QnaItemDto로 변환 (수정됨)
     */
    private QnaListRespDto.QnaItemDto convertToQnaItemDto(Qna qna) {
        // 답변 상태 판단
        String answerStatus = qna.isAnswered() ? "답변완료" : "미답변";
        
        return QnaListRespDto.QnaItemDto.builder()
                .qnaCode(qna.getQnaCode())
                .userCode(qna.getUserCode())           // 추가됨
                .productId(qna.getProductCode())       // product_code → productId
                .productName(qna.getProductName())
                .qnaTitle(qna.getQnaTitle())
                .qnaContents(qna.getQnaContents())
                .qnaWriter(qna.getQnaWriter())
                .answerStatus(answerStatus)            // "미답변" or "답변완료"
                .answerContents(qna.getAnswerContents())
                .lockStatus(qna.getLockStatus())       // 0 or 1 (수정됨)
                .createdDate(qna.getCreatedAt())
                .build();
    }
    
    /**
     * QNA 상세 조회
     */
    @Transactional
    public RespDto<QnaDetailRespDto> getQnaDetail(Integer qnaCode) {
        try {
            log.info("QNA 상세조회 시작 - qnaCode: {}", qnaCode);
            
            // QNA 조회
            Optional<Qna> qnaOpt = qnaRepository.findById(qnaCode);
            if (qnaOpt.isEmpty()) {
                log.warn("존재하지 않는 QNA - qnaCode: {}", qnaCode);
                return RespDto.<QnaDetailRespDto>builder()
                        .code(-2)
                        .build();
            }
            
            Qna qna = qnaOpt.get();
            
            // DTO 변환
            QnaDetailRespDto responseData = convertToQnaDetailDto(qna);
            
            log.info("QNA 상세조회 완료 - qnaCode: {}, title: {}", 
                    qna.getQnaCode(), qna.getQnaTitle());
            
            return RespDto.<QnaDetailRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("QNA 상세조회 실패 - qnaCode: {}", qnaCode, e);
            return RespDto.<QnaDetailRespDto>builder()
                    .code(-1)
                    .build();
        }
    }

    /**
     * Qna 엔티티를 QnaDetailRespDto로 변환
     */
    private QnaDetailRespDto convertToQnaDetailDto(Qna qna) {
        // 답변 상태 판단
        String answerStatus = qna.isAnswered() ? "답변완료" : "미답변";
        
        return QnaDetailRespDto.builder()
                .qnaCode(qna.getQnaCode())
                .userCode(qna.getUserCode())
                .productCode(qna.getProductCode())
                .productName(qna.getProductName())
                .qnaTitle(qna.getQnaTitle())
                .qnaWriter(qna.getQnaWriter())
                .qnaContents(qna.getQnaContents())
                .answerContents(qna.getAnswerContents())
                .answerAt(qna.getAnswerAt())
                .orderId(qna.getOrderId())
                .lockStatus(qna.getLockStatus())
                .createdAt(qna.getCreatedAt())
                .updatedAt(qna.getUpdatedAt())
                
                // 추가 상태 정보
                .answerStatus(answerStatus)
                .isLocked(qna.isLocked())
                .build();
    }
    
    /**
     * 상품별 QNA 목록 조회 (모든 정보 포함)
     */
    @Transactional
    public RespDto<ProductQnaListRespDto> getProductQnaList(Integer productId, Pageable pageable) {
        try {
            log.info("상품별 QNA 목록 조회 시작 - productId: {}, page: {}, size: {}", 
                    productId, pageable.getPageNumber(), pageable.getPageSize());
            
            // 상품별 QNA 목록 조회 (최신순)
            Page<Qna> qnaPage = qnaRepository.findByProductCodeOrderByCreatedAtDesc(productId, pageable);
            
            // DTO 변환 (모든 정보 포함)
            List<ProductQnaListRespDto.QnaDetailDto> qnaList = qnaPage.getContent()
                    .stream()
                    .map(this::convertToQnaDetailDto2)
                    .collect(Collectors.toList());
            
            // 페이지 정보 생성
            ProductQnaListRespDto.PaginationInfo pagination = ProductQnaListRespDto.PaginationInfo.builder()
                    .currentPage(qnaPage.getNumber())
                    .pageSize(qnaPage.getSize())
                    .totalPages(qnaPage.getTotalPages())
                    .totalElements(qnaPage.getTotalElements())
                    .hasNext(qnaPage.hasNext())
                    .hasPrevious(qnaPage.hasPrevious())
                    .build();
            
            // 응답 데이터 생성
            ProductQnaListRespDto responseData = ProductQnaListRespDto.builder()
                    .qnaList(qnaList)
                    .pagination(pagination)
                    .build();
            
            log.info("상품별 QNA 목록 조회 완료 - productId: {}, 총 {}건", 
                    productId, qnaPage.getTotalElements());
            
            return RespDto.<ProductQnaListRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("상품별 QNA 목록 조회 실패 - productId: {}", productId, e);
            return RespDto.<ProductQnaListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * QNA 생성
     */
    @Transactional
    public RespDto<String> createQna(QnaCreateReqDto reqDto) {
        try {
            log.info("QNA 생성 시작 - userCode: {}, productId: {}", 
                    reqDto.getUserCode(), reqDto.getProductId());
            
            // 사용자 존재 확인 및 이름 조회
            Optional<User> userOpt = userRepository.findByUserCode(reqDto.getUserCode());
            if (userOpt.isEmpty()) {
                log.warn("존재하지 않는 사용자 - userCode: {}", reqDto.getUserCode());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("존재하지 않는 사용자입니다.")
                        .build();
            }
            
            User user = userOpt.get();
            String writerName = user.getName(); // 또는 user.getUserName() - 실제 필드명에 맞게
            
            log.info("QNA 작성자 조회 완료 - userCode: {}, writerName: {}", 
                    reqDto.getUserCode(), writerName);
            
            // QNA 엔티티 생성
            Qna qna = Qna.builder()
                    .userCode(reqDto.getUserCode())
                    .productCode(reqDto.getProductId())
                    .productName(reqDto.getProductName())
                    .qnaTitle(reqDto.getQnaTitle())
                    .qnaWriter(writerName)
                    .qnaContents(reqDto.getQnaContents())
                    .orderId(reqDto.getOrderId())
                    .lockStatus(reqDto.getLockStatus())
                    .build();
            
            // 저장
            Qna savedQna = qnaRepository.save(qna);
            
            log.info("QNA 생성 완료 - qnaCode: {}, title: {}", 
                    savedQna.getQnaCode(), savedQna.getQnaTitle());
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("QNA가 성공적으로 등록되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("QNA 생성 실패", e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("QNA 등록 중 오류가 발생했습니다.")
                    .build();
        }
    }
    
    /**
     * QNA 수정 (내용만)
     */
    @Transactional
    public RespDto<String> updateQna(QnaUpdateReqDto reqDto) {
        try {
            log.info("QNA 수정 시작 - qnaCode: {}, userCode: {}", 
                    reqDto.getQnaCode(), reqDto.getUserCode());
            
            // QNA 조회 및 작성자 확인
            Optional<Qna> qnaOpt = qnaRepository.findByQnaCodeAndUserCode(
                    reqDto.getQnaCode(), reqDto.getUserCode());
            
            if (qnaOpt.isEmpty()) {
                log.warn("QNA를 찾을 수 없거나 권한이 없음 - qnaCode: {}, userCode: {}", 
                        reqDto.getQnaCode(), reqDto.getUserCode());
                
                // QNA 존재 여부 확인
                if (qnaRepository.existsById(reqDto.getQnaCode())) {
                    return RespDto.<String>builder()
                            .code(-3)
                            .data("본인이 작성한 QNA만 수정할 수 있습니다.")
                            .build();
                } else {
                    return RespDto.<String>builder()
                            .code(-2)
                            .data("존재하지 않는 QNA입니다.")
                            .build();
                }
            }
            
            Qna qna = qnaOpt.get();
            
            // 내용 수정
            qna.setQnaContents(reqDto.getQnaContents());
            qnaRepository.save(qna);
            
            log.info("QNA 수정 완료 - qnaCode: {}, title: {}", 
                    reqDto.getQnaCode(), qna.getQnaTitle());
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("QNA가 성공적으로 수정되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("QNA 수정 실패 - qnaCode: {}, userCode: {}", 
                    reqDto.getQnaCode(), reqDto.getUserCode(), e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("QNA 수정 중 오류가 발생했습니다.")
                    .build();
        }
    }
    
    /**
     * QNA 삭제 (작성자만, 하드 딜리트)
     */
    @Transactional
    public RespDto<String> deleteQna(Long userCode, Integer qnaCode) {
        try {
            log.info("QNA 삭제 시작 - userCode: {}, qnaCode: {}", userCode, qnaCode);
            
            // QNA 조회 및 작성자 확인
            Optional<Qna> qnaOpt = qnaRepository.findByQnaCodeAndUserCode(qnaCode, userCode);
            if (qnaOpt.isEmpty()) {
                log.warn("QNA를 찾을 수 없거나 권한이 없음 - userCode: {}, qnaCode: {}", userCode, qnaCode);
                
                // QNA 존재 여부 확인
                if (qnaRepository.existsById(qnaCode)) {
                    return RespDto.<String>builder()
                            .code(-3)
                            .data("본인이 작성한 QNA만 삭제할 수 있습니다.")
                            .build();
                } else {
                    return RespDto.<String>builder()
                            .code(-2)
                            .data("존재하지 않는 QNA입니다.")
                            .build();
                }
            }
            
            Qna qna = qnaOpt.get();
            
            // 하드 딜리트
            qnaRepository.delete(qna);
            
            log.info("QNA 삭제 완료 - qnaCode: {}, title: {}", qnaCode, qna.getQnaTitle());
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("QNA가 성공적으로 삭제되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("QNA 삭제 실패 - userCode: {}, qnaCode: {}", userCode, qnaCode, e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("QNA 삭제 중 오류가 발생했습니다.")
                    .build();
        }
    }
    
    /**
     * Qna 엔티티를 QnaDetailDto로 변환 (모든 정보 포함)
     */
    private ProductQnaListRespDto.QnaDetailDto convertToQnaDetailDto2(Qna qna) {
        String answerStatus = qna.isAnswered() ? "답변완료" : "미답변";
        
        return ProductQnaListRespDto.QnaDetailDto.builder()
                .qnaCode(qna.getQnaCode())
                .userCode(qna.getUserCode())
                .productCode(qna.getProductCode())
                .productName(qna.getProductName())
                .qnaTitle(qna.getQnaTitle())
                .qnaWriter(qna.getQnaWriter())
                .qnaContents(qna.getQnaContents())
                .answerContents(qna.getAnswerContents())
                .answerAt(qna.getAnswerAt())
                .orderId(qna.getOrderId())
                .lockStatus(qna.getLockStatus())
                .createdAt(qna.getCreatedAt())
                .updatedAt(qna.getUpdatedAt())
                
                // 추가 상태 정보
                .answerStatus(answerStatus)
                .isLocked(qna.isLocked())
                .build();
    }
    
    /**
     * QNA 답변 등록 (관리자용)
     */
    @Transactional
    public RespDto<String> answerQna(QnaAnswerReqDto reqDto) {
        try {
            log.info("QNA 답변 등록 시작 - qnaCode: {}", reqDto.getQnaCode());
            
            // QNA 조회
            Optional<Qna> qnaOpt = qnaRepository.findById(reqDto.getQnaCode());
            if (qnaOpt.isEmpty()) {
                log.warn("존재하지 않는 QNA - qnaCode: {}", reqDto.getQnaCode());
                return RespDto.<String>builder()
                        .code(-2)
                        .data("존재하지 않는 QNA입니다.")
                        .build();
            }
            
            Qna qna = qnaOpt.get();
            
            // 현재 시간 생성
            LocalDateTime now = LocalDateTime.now();
            String answerAt = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // 답변 등록
            qna.writeAnswer(reqDto.getAnswerContents(), answerAt);
            
            // 저장 (updated_at은 @UpdateTimestamp로 자동 업데이트)
            qnaRepository.save(qna);
            
            log.info("QNA 답변 등록 완료 - qnaCode: {}, title: {}", 
                    reqDto.getQnaCode(), qna.getQnaTitle());
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("답변이 성공적으로 등록되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("QNA 답변 등록 실패 - qnaCode: {}", reqDto.getQnaCode(), e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("답변 등록 중 오류가 발생했습니다.")
                    .build();
        }
    }
}