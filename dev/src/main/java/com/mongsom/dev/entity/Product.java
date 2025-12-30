package com.mongsom.dev.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;
    
    @Column(name = "base_price", nullable = false)
    private Integer basePrice;
    
    @Column(name = "sales_margin")
    private Integer salesMargin;
    
    @Column(name = "discount_per")
    private Integer discountPer;
    
    @Column(name = "discount_price")
    private Integer discountPrice;
    
    @Column(name = "delivery_price")
    private Integer deliveryPrice;
    
    @Column(name = "stock_status", nullable = false)
    @Builder.Default
    private Integer stockStatus = 1; // 0=품절, 1=주문가능, 2=부분주문가능
    
    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Integer isAvailable = 1; // 0=판매중단, 1=판매중
    
    @Column(name = "premium")
    @Builder.Default
    private Integer premium = 0; // 0=일반상품, 1=프리미엄상품
    
    @Column(name = "delete_status", nullable = false)
    @Builder.Default
    private Integer deleteStatus = 0; // 0=정상, 1=삭제
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 연관관계 매핑
    @Builder.Default
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImg> productImages = new ArrayList<>();
    
    @Builder.Default
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOptionType> optionTypes = new ArrayList<>();
    
    @Builder.Default
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOptionCombination> optionCombinations = new ArrayList<>();
    
    // 비즈니스 메서드
    public void addProductImage(ProductImg productImg) {
        if (this.productImages == null) {
            this.productImages = new ArrayList<>();
        }
        this.productImages.add(productImg);
        productImg.setProduct(this);
    }
    
    public void addOptionType(ProductOptionType optionType) {
        if (this.optionTypes == null) {
            this.optionTypes = new ArrayList<>();
        }
        this.optionTypes.add(optionType);
        optionType.setProduct(this);
    }
    
    public boolean isOrderable() {
        return stockStatus > 0 && isAvailable == 1 && deleteStatus == 0;
    }
    
    public boolean isInStock() {
        return stockStatus > 0;
    }
    
    // 업데이트 메서드
    public void updateProduct(String name, String contents, Integer basePrice, Integer premium,
                             Integer salesMargin, Integer discountPer, Integer discountPrice, 
                             Integer deliveryPrice) {
        this.name = name;
        this.contents = contents;
        this.basePrice = basePrice;
        this.premium = premium;
        this.salesMargin = salesMargin;
        this.discountPer = discountPer;
        this.discountPrice = discountPrice;
        this.deliveryPrice = deliveryPrice;
    }
    
    // 삭제 상태 관련 메서드
    public void softDelete() {
        this.deleteStatus = 1;
    }
    
    public void restore() {
        this.deleteStatus = 0;
    }
    
    public boolean isDeleted() {
        return this.deleteStatus == 1;
    }
    
    public void updateProduct(String name, String contents, Integer basePrice, 
            Integer premium, Integer salesMargin, Integer discountPer, 
            Integer discountPrice, Integer deliveryPrice,
            Integer stockStatus, Integer isAvailable) {
		this.name = name;
		this.contents = contents;
		this.basePrice = basePrice;
		this.premium = premium;
		this.salesMargin = salesMargin;
		this.discountPer = discountPer;
		this.discountPrice = discountPrice;
		this.deliveryPrice = deliveryPrice;
		this.stockStatus = stockStatus;
		this.isAvailable = isAvailable;
	}
		
		//또는 개별 업데이트 메서드들
	public void updateStockStatus(Integer stockStatus) {
		this.stockStatus = stockStatus;
	}
		
	public void updateAvailability(Integer isAvailable) {
		this.isAvailable = isAvailable;
	}
		
	public void updateStockAndAvailability(Integer stockStatus, Integer isAvailable) {
		this.stockStatus = stockStatus;
		this.isAvailable = isAvailable;
	}
}