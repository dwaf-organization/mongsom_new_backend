package com.mongsom.dev.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mongsom.dev.entity.Qna;

@Repository
public interface QnaRepository extends JpaRepository<Qna, Integer> {
    
    /**
     * 전체 QNA 목록 조회 (최신순, 페이징)
     */
    Page<Qna> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 사용자별 QNA 조회 (추가됨)
     */
    Page<Qna> findByUserCodeOrderByCreatedAtDesc(Long userCode, Pageable pageable);
    
    /**
     * 잠금 상태별 조회 (추가됨)
     */
    Page<Qna> findByLockStatusOrderByCreatedAtDesc(Integer lockStatus, Pageable pageable);
    
    /**
     * 답변 상태별 조회 (수정됨)
     */
    @Query("SELECT q FROM Qna q WHERE " +
           "(:isAnswered = true AND q.answerContents IS NOT NULL AND q.answerContents != '') OR " +
           "(:isAnswered = false AND (q.answerContents IS NULL OR q.answerContents = '')) " +
           "ORDER BY q.createdAt DESC")
    Page<Qna> findByAnswerStatus(Boolean isAnswered, Pageable pageable);
    
    /**
     * 사용자별 + 잠금 상태별 조회 (추가됨)
     */
    Page<Qna> findByUserCodeAndLockStatusOrderByCreatedAtDesc(Long userCode, Integer lockStatus, Pageable pageable);
    
    /**
     * 상품별 QNA 조회
     */
    Page<Qna> findByProductCodeOrderByCreatedAtDesc(Integer productCode, Pageable pageable);
    
    /**
     * 사용자별 QNA 상세조회 (권한 확인용)
     */
    Optional<Qna> findByQnaCodeAndUserCode(Integer qnaCode, Long userCode);

    /**
     * 상품별 QNA 상세조회
     */
    Optional<Qna> findByQnaCodeAndProductCode(Integer qnaCode, Integer productCode);

    /**
     * QNA 조회 (연관 엔티티 포함)
     */
    @Query("SELECT q FROM Qna q " +
           "LEFT JOIN FETCH q.user u " +
           "LEFT JOIN FETCH q.product p " +
           "WHERE q.qnaCode = :qnaCode")
    Optional<Qna> findByIdWithDetails(@Param("qnaCode") Integer qnaCode);
    
    /**
     * QNA 존재 여부 확인
     */
    boolean existsByQnaCode(Integer qnaCode);
    
}