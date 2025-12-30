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
@Table(name = "order_detail_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailOption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_option_id")
    private Integer orderDetailOptionId;
    
    @Column(name = "order_detail_id", nullable = false)
    private Integer orderDetailId;
    
    @Column(name = "option_type_name", nullable = false, length = 100)
    private String optionTypeName; // 주문 당시의 옵션 타입명 (예: "용량", "색상")
    
    @Column(name = "option_value_name", nullable = false, length = 100)
    private String optionValueName; // 주문 당시의 옵션값 (예: "256GB", "블랙")
    
    @Column(name = "price_adjustment", nullable = false)
    @Builder.Default
    private Integer priceAdjustment = 0; // 주문 당시의 가격 조정값
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", insertable = false, updatable = false)
    private OrderDetail orderDetail;
    
    // 정적 팩토리 메서드
    public static OrderDetailOption createOrderDetailOption(Integer orderDetailId, 
                                                           String optionTypeName, 
                                                           String optionValueName, 
                                                           Integer priceAdjustment) {
        return OrderDetailOption.builder()
                .orderDetailId(orderDetailId)
                .optionTypeName(optionTypeName)
                .optionValueName(optionValueName)
                .priceAdjustment(priceAdjustment)
                .build();
    }
}