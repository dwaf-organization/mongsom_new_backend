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
import java.util.ArrayList;
import java.util.List;

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
    
    @Column(name = "combination_id")
    private Integer combinationId; // 옵션 조합 ID
    
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;
    
    @Column(name = "base_price", nullable = false)
    private Integer basePrice; // 담을 당시 상품 기본 가격
    
    @Column(name = "option_price")
    @Builder.Default
    private Integer optionPrice = 0; // 옵션 추가 가격
    
    @Column(name = "total_unit_price", nullable = false)
    private Integer totalUnitPrice; // 개당 총 가격 (base_price + option_price)
    
    @Column(name = "check_status", nullable = false)
    @Builder.Default
    private Integer checkStatus = 1;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", insertable = false, updatable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combination_id", insertable = false, updatable = false)
    private ProductOptionCombination optionCombination;
    
    @Builder.Default
    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartOptionSelection> optionSelections = new ArrayList<>();
    
    // 비즈니스 메서드
    public void addOptionSelection(CartOptionSelection selection) {
        if (this.optionSelections == null) {
            this.optionSelections = new ArrayList<>();
        }
        this.optionSelections.add(selection);
        selection.setCart(this);
    }
    
    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public void updateCheckStatus(Integer checkStatus) {
        this.checkStatus = checkStatus;
    }
    
    // 가격 계산 메서드
    public void calculateTotalPrice() {
        this.totalUnitPrice = this.basePrice + this.optionPrice;
    }
    
    public Integer getTotalLinePrice() {
        return this.totalUnitPrice * this.quantity;
    }
    
    public void updatePrices(Integer basePrice, Integer optionPrice) {
        this.basePrice = basePrice;
        this.optionPrice = optionPrice;
        calculateTotalPrice();
    }
    
    // 상태 확인 메서드
    public boolean isChecked() {
        return this.checkStatus == 1;
    }
    
}