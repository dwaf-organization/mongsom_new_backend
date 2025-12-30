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
@Table(name = "product_option_type")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_type_id")
    private Integer optionTypeId;
    
    @Column(name = "product_id", nullable = false)
    private Integer productId;
    
    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName; // "색상", "사이즈"
    
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
    
    @Column(name = "is_required")
    @Builder.Default
    private Integer isRequired = 1; // 0=선택, 1=필수
    
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
    @OneToMany(mappedBy = "optionType", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOptionValue> optionValues = new ArrayList<>();
    
    // 비즈니스 메서드
    public void setProductId(Integer productId) {
        this.productId = productId;
    }
    
    public void setProduct(Product product) {
        this.product = product;
        this.productId = product.getProductId();
    }
    
    public void addOptionValue(ProductOptionValue optionValue) {
        if (this.optionValues == null) {
            this.optionValues = new ArrayList<>();
        }
        this.optionValues.add(optionValue);
        optionValue.setOptionType(this);
    }
    
    public void updateTypeName(String typeName) {
        this.typeName = typeName;
    }
    
    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public boolean isRequiredOption() {
        return this.isRequired == 1;
    }
    
    // 소프트 삭제 필드 추가
    @Column(name = "is_deleted")
    @Builder.Default
    private Integer isDeleted = 0; // 0=정상, 1=삭제

    // 소프트 삭제 메서드
    public void softDelete() {
        this.isDeleted = 1;
    }
    
}