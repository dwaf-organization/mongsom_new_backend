package com.mongsom.dev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mongsom.dev.entity.ProductImg;

@Repository
public interface ProductImgRepository extends JpaRepository<ProductImg, Integer> {
    
    // 특정 상품의 이미지들 조회
    @Query("SELECT pi FROM ProductImg pi WHERE pi.productId = :productId ORDER BY pi.createdAt")
    List<ProductImg> findByProductId(@Param("productId") Integer productId);
    
    // 여러 상품의 이미지들 조회 (N+1 해결용)
    @Query("SELECT pi FROM ProductImg pi WHERE pi.productId IN :productIds ORDER BY pi.productId, pi.createdAt")
    List<ProductImg> findByProductIdInOrderByProductIdAndCreatedAt(@Param("productIds") List<Integer> productIds);
    
    // 상품별 이미지 URL들만 조회
    @Query(value = "SELECT product_img_url FROM product_img WHERE product_id = :productId ORDER BY created_at", nativeQuery = true)
    List<String> findImgUrlsByProductId(@Param("productId") Integer productId);
    
    // 상품의 이미지 개수 조회
    @Query("SELECT COUNT(pi) FROM ProductImg pi WHERE pi.productId = :productId")
    Long countByProductId(@Param("productId") Integer productId);
    
    // 상품의 첫 번째 이미지 조회
    @Query("SELECT pi FROM ProductImg pi WHERE pi.productId = :productId ORDER BY pi.createdAt LIMIT 1")
    ProductImg findFirstByProductId(@Param("productId") Integer productId);
    
    // 특정 이미지 URL 존재 여부 확인
    @Query("SELECT COUNT(pi) > 0 FROM ProductImg pi WHERE pi.productImgUrl = :imageUrl")
    boolean existsByProductImgUrl(@Param("imageUrl") String imageUrl);
    
    // 상품 ID로 이미지 전체 삭제
    @Modifying
    @Query("DELETE FROM ProductImg p WHERE p.productId = :productId")
    void deleteByProductId(@Param("productId") Integer productId);
    
    // 상품 ID와 이미지 URL로 존재 여부 확인
    boolean existsByProductIdAndProductImgUrl(Integer productId, String productImgUrl);
}