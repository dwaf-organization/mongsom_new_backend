package com.mongsom.dev.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;
    
    @Column(name = "user_code", nullable = false)
    private Long userCode;
    
    @Column(name = "order_detail_id", nullable = false)
    private Integer orderDetailId;
    
    @Column(name = "product_id", nullable = false)
    private Integer productId;
    
    @Column(name = "order_id", nullable = false)
    private Integer orderId;
    
    @Column(name = "review_rating")
    private Integer reviewRating; // NULL 허용
    
    @Lob
    @Column(name = "review_content", columnDefinition = "TEXT")
    private String reviewContent; // NULL 허용
    
    @Column(name = "admin_hidden", nullable = false)
    @Builder.Default
    private Integer adminHidden = 0; // 0=정상, 1=관리자 임의숨김
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", insertable = false, updatable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", insertable = false, updatable = false)
    private OrderDetail orderDetail;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    // 리뷰 이미지가 있다면 (별도 테이블)
    @OneToMany(mappedBy = "reviewId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReviewImg> reviewImages;
    
    // 비즈니스 메서드
    
    /**
     * 숨김 처리 (관리자)
     */
    public void hideByAdmin() {
        this.adminHidden = 1;
    }
    
    /**
     * 숨김 해제 (관리자)
     */
    public void showByAdmin() {
        this.adminHidden = 0;
    }
    
    /**
     * 숨김 상태인지 확인
     */
    public boolean isHidden() {
        return adminHidden == 1;
    }
    
    /**
     * 정상 상태인지 확인
     */
    public boolean isVisible() {
        return adminHidden == 0;
    }
    
    /**
     * 숨김 상태 토글
     */
    public void toggleHiddenStatus() {
        this.adminHidden = this.adminHidden == 1 ? 0 : 1;
    }
    
    /**
     * 리뷰 평점이 유효한지 확인
     */
    public boolean isValidRating() {
        return reviewRating != null && reviewRating >= 1 && reviewRating <= 5;
    }
    
    /**
     * 리뷰 내용이 있는지 확인
     */
    public boolean hasContent() {
        return reviewContent != null && !reviewContent.trim().isEmpty();
    }
    
    /**
     * 리뷰 상태 요약
     */
    public String getStatusSummary() {
        return isHidden() ? "관리자 숨김" : "정상";
    }
    
    /**
     * 정적 팩토리 메서드 - 리뷰 생성
     */
    public static UserReview createReview(Long userCode, Integer orderDetailId, Integer productId, 
                                         Integer orderId, Integer rating, String content) {
        return UserReview.builder()
                .userCode(userCode)
                .orderDetailId(orderDetailId)
                .productId(productId)
                .orderId(orderId)
                .reviewRating(rating)
                .reviewContent(content)
                .adminHidden(0)
                .build();
    }
}