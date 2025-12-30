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
@Table(name = "order_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Integer orderDetailId;
    
    @Column(name = "order_id", nullable = false)
    private Integer orderId;
    
    @Column(name = "user_code", nullable = false)
    private Long userCode;
    
    @Column(name = "product_id", nullable = false)
    private Integer productId;
    
    @Column(name = "combination_id")
    private Integer combinationId; // 옵션 조합 ID (NULL 허용)
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "base_price", nullable = false)
    private Integer basePrice; // 주문 당시 상품 기본가격
    
    @Column(name = "option_price")
    @Builder.Default
    private Integer optionPrice = 0; // 옵션 추가가격
    
    @Column(name = "unit_total_price", nullable = false)
    private Integer unitTotalPrice; // 개당 총가격 (base_price + option_price)
    
    @Column(name = "line_total_price", nullable = false)
    private Integer lineTotalPrice; // 라인 총가격 (unit_total_price * quantity)
    
    @Column(name = "review_status")
    @Builder.Default
    private Integer reviewStatus = 0; // 0=작성안함, 1=작성완료
    
    @Column(name = "order_status")
    @Builder.Default
    private Integer orderStatus = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private OrderItem orderItem;
    
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
    @OneToMany(mappedBy = "orderDetailId", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetailOption> orderDetailOptions = new ArrayList<>();
    
    // 비즈니스 메서드
    public void addOrderDetailOption(OrderDetailOption orderDetailOption) {
        if (this.orderDetailOptions == null) {
            this.orderDetailOptions = new ArrayList<>();
        }
        this.orderDetailOptions.add(orderDetailOption);
        orderDetailOption.setOrderDetailId(this.orderDetailId);
    }
    
    // 가격 계산 메서드
    public void calculatePrices() {
        this.unitTotalPrice = this.basePrice + this.optionPrice;
        this.lineTotalPrice = this.unitTotalPrice * this.quantity;
    }
    
    public void updatePrices(Integer basePrice, Integer optionPrice) {
        this.basePrice = basePrice;
        this.optionPrice = optionPrice;
        calculatePrices();
    }
    
    // 리뷰 관련 메서드
    public void markReviewWritten() {
        this.reviewStatus = 1;
    }
    
    public void markReviewNotWritten() {
        this.reviewStatus = 0;
    }
    
    public boolean isReviewWritten() {
        return this.reviewStatus == 1;
    }
    
    public boolean canWriteReview() {
        return this.reviewStatus == 0 && orderItem != null && "배송완료".equals(orderItem.getDeliveryStatus());
    }
    
    // 옵션 관련 메서드
    public boolean hasOptions() {
        return combinationId != null || (orderDetailOptions != null && !orderDetailOptions.isEmpty());
    }
    
    // 주문 상태 관련 메서드
    public boolean isNormalOrder() {
        return orderStatus == 0;
    }
    
    // 정적 팩토리 메서드
    public static OrderDetail createOrderDetail(Integer orderId, Long userCode, Integer productId,
                                               Integer combinationId, Integer quantity, Integer basePrice, Integer optionPrice) {
        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(orderId)
                .userCode(userCode)
                .productId(productId)
                .combinationId(combinationId)
                .quantity(quantity)
                .basePrice(basePrice)
                .optionPrice(optionPrice)
                .build();
        
        orderDetail.calculatePrices();
        return orderDetail;
    }
}