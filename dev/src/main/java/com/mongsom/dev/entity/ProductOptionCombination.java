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
@Table(name = "product_option_combination")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionCombination {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "combination_id")
    private Integer combinationId;
    
    @Column(name = "product_id", nullable = false)
    private Integer productId;
    
    @Column(name = "combination_key", nullable = false, length = 200)
    private String combinationKey; // "color_red_size_L"
    
    @Column(name = "stock_quantity")
    @Builder.Default
    private Integer stockQuantity = 0;
    
    @Column(name = "reserved_quantity")
    @Builder.Default
    private Integer reservedQuantity = 0;
    
    // available_quantity는 DB에서 generated column으로 계산됨
    @Column(name = "available_quantity", insertable = false, updatable = false)
    private Integer availableQuantity;
    
    @Column(name = "stock_status")
    @Builder.Default
    private Integer stockStatus = 1; // 0=품절, 1=주문가능
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 연관관계 매핑
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    @Builder.Default
    @OneToMany(mappedBy = "combination", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionCombinationMapping> mappings = new ArrayList<>();
    
    // 비즈니스 메서드
    public void setProduct(Product product) {
        this.product = product;
        this.productId = product.getProductId();
    }
    
    public void addMapping(OptionCombinationMapping mapping) {
        if (this.mappings == null) {
            this.mappings = new ArrayList<>();
        }
        this.mappings.add(mapping);
        mapping.setCombination(this);
    }
    
    // 재고 관리 메서드
    public void updateStock(Integer quantity) {
        this.stockQuantity = quantity;
        updateStockStatus();
    }
    
    public void increaseStock(Integer quantity) {
        this.stockQuantity += quantity;
        updateStockStatus();
    }
    
    public void decreaseStock(Integer quantity) {
        this.stockQuantity = Math.max(0, this.stockQuantity - quantity);
        updateStockStatus();
    }
    
    public void reserveStock(Integer quantity) {
        if (getAvailableQuantity() >= quantity) {
            this.reservedQuantity += quantity;
            updateStockStatus();
        } else {
            throw new IllegalArgumentException("재고가 부족합니다. 요청수량: " + quantity + ", 가능수량: " + getAvailableQuantity());
        }
    }
    
    public void releaseReserved(Integer quantity) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
        updateStockStatus();
    }
    
    public void confirmOrder(Integer quantity) {
        // 예약된 재고를 실제 재고에서 차감
        this.stockQuantity = Math.max(0, this.stockQuantity - quantity);
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
        updateStockStatus();
    }
    
    private void updateStockStatus() {
        this.stockStatus = (getAvailableQuantity() > 0) ? 1 : 0;
    }
    
    public Integer getAvailableQuantity() {
        return stockQuantity - reservedQuantity;
    }
    
    public boolean isInStock() {
        return stockStatus == 1 && getAvailableQuantity() > 0;
    }
    
    public boolean isOutOfStock() {
        return stockStatus == 0 || getAvailableQuantity() <= 0;
    }
    
    public boolean canOrder(Integer quantity) {
        return isInStock() && getAvailableQuantity() >= quantity;
    }
    
    // 조합 키 생성 메서드
    public static String generateCombinationKey(List<String> optionValues) {
        return String.join("_", optionValues).toLowerCase().replaceAll("\\s+", "");
    }
}