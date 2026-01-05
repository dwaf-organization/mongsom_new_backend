package com.mongsom.dev.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "order_detail")
@Getter
@Setter  // Setter 추가
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
    
    @Column(name = "option1")
    private Integer option1; // 첫 번째 옵션 (NULL 허용)
    
    @Column(name = "option2")  
    private Integer option2; // 두 번째 옵션 (NULL 허용)
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "base_price", nullable = false)
    private Integer basePrice; // 기본 가격
    
    @Column(name = "option_price", nullable = false)
    @Builder.Default
    private Integer optionPrice = 0; // 옵션 추가 가격
    
    @Column(name = "unit_total_price", nullable = false)
    private Integer unitTotalPrice; // 개당 총 가격 (basePrice + optionPrice)
    
    @Column(name = "line_total_price", nullable = false)
    private Integer lineTotalPrice; // 라인 총 가격 (unitTotalPrice * quantity)
    
    @Column(name = "review_status")
    @Builder.Default
    private Integer reviewStatus = 0; // 0=작성안함, 1=작성완료
    
    @Column(name = "order_status")
    @Builder.Default
    private Integer orderStatus = 0; // 0=정상, 1=취소 등
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 연관관계 (주문 이력이므로 외래키 제약 없음)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private OrderItem orderItem;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    // 옵션 이력 (OrderDetailOption 테이블 사용 시)
    @OneToMany(mappedBy = "orderDetailId", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetailOption> orderDetailOptions;
    
    // 비즈니스 메서드
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
    public void calculatePrices() {
        this.unitTotalPrice = this.basePrice + this.optionPrice;
        this.lineTotalPrice = this.unitTotalPrice * this.quantity;
    }
    
    // 정적 팩토리 메서드 - Cart에서 OrderDetail 생성
    public static OrderDetail fromCart(Cart cart, Integer orderId, Integer basePrice, Integer optionPrice) {
        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(orderId)
                .productId(cart.getProductId())
                .option1(cart.getOption1())
                .option2(cart.getOption2())
                .quantity(cart.getQuantity())
                .basePrice(basePrice)
                .optionPrice(optionPrice)
                .orderStatus(0)
                .build();
        
        // 가격 계산
        orderDetail.calculatePrices();
        
        return orderDetail;
    }
    
    // 정적 팩토리 메서드 - 직접 생성
    public static OrderDetail createOrderDetail(Integer orderId, Long userCode, Integer productId, 
                                              Integer option1, Integer option2,
                                              Integer quantity, Integer basePrice, Integer optionPrice) {
        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(orderId)
                .userCode(userCode)
                .productId(productId)
                .option1(option1)
                .option2(option2)
                .quantity(quantity)
                .basePrice(basePrice)
                .optionPrice(optionPrice)
                .orderStatus(0)
                .build();
        
        orderDetail.calculatePrices();
        
        return orderDetail;
    }
    
    // 옵션 정보 요약 (문자열) - 수정된 버전
    public String getOptionSummary() {
        StringBuilder sb = new StringBuilder();
        
        if (hasOption1() && orderDetailOptions != null) {
            orderDetailOptions.stream()
                    .filter(opt -> opt.getOptionValueId().equals(option1))  // OrderDetailOption에 getOptionValueId() 메서드 필요
                    .findFirst()
                    .ifPresent(opt -> sb.append(opt.getOptionTypeName()).append(": ").append(opt.getOptionValueName()));
        }
        
        if (hasOption2() && orderDetailOptions != null) {
            if (sb.length() > 0) sb.append(", ");
            orderDetailOptions.stream()
                    .filter(opt -> opt.getOptionValueId().equals(option2))
                    .findFirst()
                    .ifPresent(opt -> sb.append(opt.getOptionTypeName()).append(": ").append(opt.getOptionValueName()));
        }
        
        return sb.toString();
    }
    
    // 간단한 옵션 정보 요약 (OrderDetailOption 테이블 없이)
    public String getSimpleOptionSummary() {
        StringBuilder sb = new StringBuilder();
        
        // ProductOptionValueRepository를 통해 옵션 정보 조회하는 방식으로 변경 필요
        // 또는 Service 레벨에서 처리
        
        return sb.toString();
    }
}