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
    
    @Column(name = "product_id", nullable = false)
    private Integer productId;
    
    @Column(name = "option1")
    private Integer option1; // 첫 번째 옵션 (NULL 허용)
    
    @Column(name = "option2")
    private Integer option2; // 두 번째 옵션 (NULL 허용)
    
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;
    
    @Column(name = "base_price", nullable = false)
    private Integer basePrice; // 상품 기본 가격 (할인된 가격)
    
    @Column(name = "option_price")
    @Builder.Default
    private Integer optionPrice = 0; // 옵션 추가 가격 (option1 + option2의 price_adjustment)
    
    @Column(name = "total_unit_price", nullable = false)
    private Integer totalUnitPrice; // 개당 총 가격 (base_price + option_price)
    
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
    @JoinColumn(name = "option1", insertable = false, updatable = false)
    private ProductOptionValue optionValue1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option2", insertable = false, updatable = false)
    private ProductOptionValue optionValue2;
    
    // 비즈니스 메서드
    public void updateQuantity(Integer quantity) {
        if (quantity > 0) {
            this.quantity = quantity;
        }
    }
    
    public void increaseQuantity(Integer amount) {
        this.quantity += amount;
    }
    
    public void updatePrices(Integer basePrice, Integer optionPrice) {
        this.basePrice = basePrice;
        this.optionPrice = optionPrice;
        this.totalUnitPrice = basePrice + optionPrice;
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
    
    public boolean hasOption1() {
        return option1 != null;
    }
    
    public boolean hasOption2() {
        return option2 != null;
    }
    
    public boolean hasAnyOptions() {
        return hasOption1() || hasOption2();
    }
    
    // 가격 계산 메서드
    public Integer getLineTotalPrice() {
        return this.totalUnitPrice * this.quantity;
    }
    
    // 옵션 동일성 체크
    public boolean isSameOptions(Integer option1, Integer option2) {
        return java.util.Objects.equals(this.option1, option1) && 
               java.util.Objects.equals(this.option2, option2);
    }
    
    // 정적 팩토리 메서드 (가격 계산 포함)
    public static Cart createCart(Long userCode, Integer productId, Integer option1, Integer option2, 
                                 Integer quantity, Integer basePrice, Integer optionPrice) {
        return Cart.builder()
                .userCode(userCode)
                .productId(productId)
                .option1(option1)
                .option2(option2)
                .quantity(quantity)
                .basePrice(basePrice)
                .optionPrice(optionPrice)
                .totalUnitPrice(basePrice + optionPrice)
                .checkStatus(1)
                .build();
    }
    
    // 옵션 없는 상품용
    public static Cart createCartWithoutOptions(Long userCode, Integer productId, Integer quantity, Integer basePrice) {
        return createCart(userCode, productId, null, null, quantity, basePrice, 0);
    }
    
}