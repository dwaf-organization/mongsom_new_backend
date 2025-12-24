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
@Table(name = "option_combination_mapping",
       uniqueConstraints = @UniqueConstraint(columnNames = {"combination_id", "option_value_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionCombinationMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Integer mappingId;
    
    @Column(name = "combination_id", nullable = false)
    private Integer combinationId;
    
    @Column(name = "option_value_id", nullable = false)
    private Integer optionValueId;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combination_id", insertable = false, updatable = false)
    private ProductOptionCombination combination;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_value_id", insertable = false, updatable = false)
    private ProductOptionValue optionValue;
    
    // 비즈니스 메서드
    public void setCombination(ProductOptionCombination combination) {
        this.combination = combination;
        this.combinationId = combination.getCombinationId();
    }
    
    public void setOptionValue(ProductOptionValue optionValue) {
        this.optionValue = optionValue;
        this.optionValueId = optionValue.getOptionValueId();
    }
}