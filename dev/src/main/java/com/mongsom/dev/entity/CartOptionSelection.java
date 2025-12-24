package com.mongsom.dev.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_option_selection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartOptionSelection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_option_id")
    private Integer cartOptionId;
    
    @Column(name = "cart_id", nullable = false)
    private Integer cartId;
    
    @Column(name = "option_type_id", nullable = false)
    private Integer optionTypeId;
    
    @Column(name = "option_value_id", nullable = false)
    private Integer optionValueId;
    
    @Column(name = "selected_value", nullable = false, length = 100)
    private String selectedValue; // "빨강", "L" 등
    
    @Column(name = "price_adjustment")
    @Builder.Default
    private Integer priceAdjustment = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", insertable = false, updatable = false)
    private Cart cart;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_type_id", insertable = false, updatable = false)
    private ProductOptionType optionType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_value_id", insertable = false, updatable = false)
    private ProductOptionValue optionValue;
    
    // 비즈니스 메서드
    public void setCart(Cart cart) {
        this.cart = cart;
        this.cartId = cart.getCartId();
    }
    
    public void setOptionType(ProductOptionType optionType) {
        this.optionType = optionType;
        this.optionTypeId = optionType.getOptionTypeId();
    }
    
    public void setOptionValue(ProductOptionValue optionValue) {
        this.optionValue = optionValue;
        this.optionValueId = optionValue.getOptionValueId();
        this.selectedValue = optionValue.getValueName();
        this.priceAdjustment = optionValue.getPriceAdjustment();
    }
    
    public void updateSelectedValue(String selectedValue, Integer priceAdjustment) {
        this.selectedValue = selectedValue;
        this.priceAdjustment = priceAdjustment;
    }
}