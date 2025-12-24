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

@Entity
@Table(name = "product_img")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImg {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_img_id")
    private Integer productImgId;
    
    @Column(name = "product_id", nullable = false)
    private Integer productId;
    
    @Column(name = "product_img_url", nullable = false, length = 500)
    private String productImgUrl;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    // 비즈니스 메서드
    public void setProduct(Product product) {
        this.product = product;
        this.productId = product.getProductId();
    }
    
    public void updateImageUrl(String imageUrl) {
        this.productImgUrl = imageUrl;
    }
    
    // 정적 팩토리 메서드
    public static ProductImg createProductImg(Integer productId, String imageUrl) {
        return ProductImg.builder()
                .productId(productId)
                .productImgUrl(imageUrl)
                .build();
    }
    
    // 이미지 URL 검증 메서드
    public boolean isValidImageUrl() {
        return productImgUrl != null && !productImgUrl.trim().isEmpty() &&
               (productImgUrl.startsWith("http://") || productImgUrl.startsWith("https://"));
    }
}