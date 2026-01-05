package com.mongsom.dev.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "qna")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Qna {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_code")
    private Integer qnaCode;
    
    @Column(name = "user_code", nullable = false)
    private Long userCode; // 추가됨
    
    @Column(name = "product_code")
    private Integer productCode;
    
    @Column(name = "product_name")
    private String productName;
    
    @Column(name = "qna_title", nullable = false, length = 500)
    private String qnaTitle;
    
    @Column(name = "qna_writer", nullable = false, length = 100)
    private String qnaWriter;
    
    @Lob
    @Column(name = "qna_contents", nullable = false, columnDefinition = "TEXT")
    private String qnaContents;
    
    @Lob
    @Column(name = "answer_contents", columnDefinition = "TEXT")
    private String answerContents;
    
    @Column(name = "answer_at", length = 50)
    private String answerAt;
    
    @Column(name = "order_id", length = 100)
    private String orderId;
    
    @Column(name = "lock_status", nullable = false)
    @Builder.Default
    private Integer lockStatus = 0; // 추가됨: 0=공개, 1=비밀글
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", insertable = false, updatable = false)
    private User user; // 추가됨
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code", insertable = false, updatable = false)
    private Product product;
    
    // 비즈니스 메서드
    
    /**
     * 답변 여부 확인
     */
    public boolean isAnswered() {
        return answerContents != null && !answerContents.trim().isEmpty();
    }
    
    /**
     * 비밀글 여부 확인
     */
    public boolean isLocked() {
        return lockStatus == 1;
    }
    
    /**
     * 공개글 여부 확인
     */
    public boolean isPublic() {
        return lockStatus == 0;
    }
    
    /**
     * 상품 관련 문의 여부 확인
     */
    public boolean isProductRelated() {
        return productCode != null;
    }
    
    /**
     * 주문 관련 문의 여부 확인
     */
    public boolean isOrderRelated() {
        return orderId != null && !orderId.trim().isEmpty();
    }
    
    /**
     * 답변 작성
     */
    public void writeAnswer(String answerContents, String answerAt) {
        this.answerContents = answerContents;
        this.answerAt = answerAt;
    }
    
    /**
     * 답변 삭제
     */
    public void deleteAnswer() {
        this.answerContents = null;
        this.answerAt = null;
    }
    
    /**
     * 비밀글로 변경
     */
    public void makeLocked() {
        this.lockStatus = 1;
    }
    
    /**
     * 공개글로 변경
     */
    public void makePublic() {
        this.lockStatus = 0;
    }
    
    /**
     * 잠금 상태 토글
     */
    public void toggleLockStatus() {
        this.lockStatus = this.lockStatus == 1 ? 0 : 1;
    }
    
    /**
     * 문의 상태 요약
     */
    public String getStatusSummary() {
        StringBuilder status = new StringBuilder();
        
        if (isAnswered()) {
            status.append("답변완료");
        } else {
            status.append("미답변");
        }
        
        if (isLocked()) {
            status.append(" (비밀글)");
        } else {
            status.append(" (공개글)");
        }
        
        return status.toString();
    }
    
    // 정적 팩토리 메서드
    
    /**
     * 공개 상품 문의 생성
     */
    public static Qna createPublicProductQna(Long userCode, Integer productCode, String productName, 
                                           String title, String writer, String contents) {
        return Qna.builder()
                .userCode(userCode)
                .productCode(productCode)
                .productName(productName)
                .qnaTitle(title)
                .qnaWriter(writer)
                .qnaContents(contents)
                .lockStatus(0)
                .build();
    }
    
    /**
     * 비밀 상품 문의 생성
     */
    public static Qna createLockedProductQna(Long userCode, Integer productCode, String productName,
                                           String title, String writer, String contents) {
        return Qna.builder()
                .userCode(userCode)
                .productCode(productCode)
                .productName(productName)
                .qnaTitle(title)
                .qnaWriter(writer)
                .qnaContents(contents)
                .lockStatus(1)
                .build();
    }
    
    /**
     * 주문 관련 문의 생성
     */
    public static Qna createOrderQna(Long userCode, String orderId, String title, 
                                   String writer, String contents, Integer lockStatus) {
        return Qna.builder()
                .userCode(userCode)
                .orderId(orderId)
                .qnaTitle(title)
                .qnaWriter(writer)
                .qnaContents(contents)
                .lockStatus(lockStatus)
                .build();
    }
    
    /**
     * 일반 문의 생성
     */
    public static Qna createGeneralQna(Long userCode, String title, String writer, 
                                     String contents, Integer lockStatus) {
        return Qna.builder()
                .userCode(userCode)
                .qnaTitle(title)
                .qnaWriter(writer)
                .qnaContents(contents)
                .lockStatus(lockStatus)
                .build();
    }
}