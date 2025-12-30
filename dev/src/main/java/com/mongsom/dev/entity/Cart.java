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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Integer cartId;
    
    @Column(name = "user_code", nullable = false)
    private Long userCode;
    
    @Column(name = "combination_id")
    private Integer combinationId; // 새로운 옵션 조합 ID (NULL 허용)
    
    @Column(name = "product_id", nullable = false)
    private Integer productId;
    
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;
    
    @Column(name = "check_status", nullable = false)
    @Builder.Default
    private Integer checkStatus = 1; // 0=체크해제, 1=체크됨
    
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
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combination_id", insertable = false, updatable = false)
    private ProductOptionCombination optionCombination;
    
    // 비즈니스 메서드
    public void updateQuantity(Integer quantity) {
        if (quantity > 0) {
            this.quantity = quantity;
        }
    }
    
    public void increaseQuantity(Integer amount) {
        this.quantity += amount;
    }
    
    public void toggleCheckStatus() {
        this.checkStatus = this.checkStatus == 1 ? 0 : 1;
    }
    
    public void setChecked() {
        this.checkStatus = 1;
    }
    
    public void setUnchecked() {
        this.checkStatus = 0;
    }
    
    public boolean isChecked() {
        return this.checkStatus == 1;
    }
    
    public boolean hasOptions() {
        return combinationId != null;
    }
    
    // 정적 팩토리 메서드
    public static Cart createCart(Long userCode, Integer productId, Integer combinationId, Integer quantity) {
        return Cart.builder()
                .userCode(userCode)
                .productId(productId)
                .combinationId(combinationId)
                .quantity(quantity)
                .checkStatus(1)
                .build();
    }
}