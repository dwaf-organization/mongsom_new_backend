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
    
    // 조합 키 생성 메서드
    public static String generateCombinationKey(List<String> optionValues) {
        return String.join("_", optionValues).toLowerCase().replaceAll("\\s+", "");
    }
    
    // 기존 엔티티에 소프트 삭제 필드 추가
    @Column(name = "is_deleted")
    @Builder.Default
    private Integer isDeleted = 0; // 0=정상, 1=삭제

    // 소프트 삭제 메서드 추가
    public void softDelete() {
        this.isDeleted = 1;
    }

    public boolean isDeleted() {
        return this.isDeleted == 1;
    }
}