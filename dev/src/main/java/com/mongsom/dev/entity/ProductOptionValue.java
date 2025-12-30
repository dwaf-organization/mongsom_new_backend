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

@Entity
@Table(name = "product_option_value")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionValue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_value_id")
    private Integer optionValueId;
    
    @Column(name = "option_type_id", nullable = false)
    private Integer optionTypeId;
    
    @Column(name = "value_name", nullable = false, length = 50)
    private String valueName; // "빨강", "L"
    
    @Column(name = "price_adjustment")
    @Builder.Default
    private Integer priceAdjustment = 0; // 추가/차감 가격
    
    @Column(name = "stock_status")
    @Builder.Default
    private Integer stockStatus = 1; // 0=품절, 1=주문가능
    
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 연관관계 매핑
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_type_id", insertable = false, updatable = false)
    private ProductOptionType optionType;
    
    // 비즈니스 메서드
    public void setOptionTypeId(Integer optionTypeId) {
        this.optionTypeId = optionTypeId;
    }
    
    public void setOptionType(ProductOptionType optionType) {
        this.optionType = optionType;
        this.optionTypeId = optionType.getOptionTypeId();
    }
    
    public void updateValueName(String valueName) {
        this.valueName = valueName;
    }
    
    public void updatePriceAdjustment(Integer priceAdjustment) {
        this.priceAdjustment = priceAdjustment;
    }
    
    public void updateStockStatus(Integer stockStatus) {
        this.stockStatus = stockStatus;
    }
    
    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public void updateAll(String valueName, Integer priceAdjustment, Integer stockStatus, Integer sortOrder) {
        this.valueName = valueName;
        this.priceAdjustment = priceAdjustment;
        this.stockStatus = stockStatus;
        this.sortOrder = sortOrder;
    }
    
    // 가격 관련 메서드
    public Integer calculatePrice(Integer basePrice) {
        return basePrice + priceAdjustment;
    }
    
    public boolean hasAdditionalCost() {
        return priceAdjustment > 0;
    }
    
    public boolean hasDiscount() {
        return priceAdjustment < 0;
    }
    
    // 소프트 삭제 필드 추가 (기존에 있다면 메서드만 추가)
    @Column(name = "is_deleted")
    @Builder.Default
    private Integer isDeleted = 0; // 0=정상, 1=삭제

    public void softDelete() {
        this.isDeleted = 1;
    }
    
}