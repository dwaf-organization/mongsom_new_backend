package com.mongsom.dev.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_detail_option")
@Getter
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
    
    @Column(name = "option_value_id", nullable = false)  // 이 필드 추가 필요
    private Integer optionValueId;
    
    @Column(name = "option_type_name", nullable = false)
    private String optionTypeName;
    
    @Column(name = "option_value_name", nullable = false)
    private String optionValueName;
    
    @Column(name = "price_adjustment", nullable = false)
    @Builder.Default
    private Integer priceAdjustment = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", insertable = false, updatable = false)
    private OrderDetail orderDetail;
    
    // 정적 팩토리 메서드
    public static OrderDetailOption createFromOptionValue(Integer orderDetailId, 
                                                         Integer optionValueId,
                                                         String optionTypeName, 
                                                         String optionValueName, 
                                                         Integer priceAdjustment) {
        return OrderDetailOption.builder()
                .orderDetailId(orderDetailId)
                .optionValueId(optionValueId)  // 추가
                .optionTypeName(optionTypeName)
                .optionValueName(optionValueName)
                .priceAdjustment(priceAdjustment)
                .build();
    }
}