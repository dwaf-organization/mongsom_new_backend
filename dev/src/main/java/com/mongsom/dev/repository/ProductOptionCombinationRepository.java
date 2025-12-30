package com.mongsom.dev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mongsom.dev.entity.ProductOptionCombination;

@Repository
public interface ProductOptionCombinationRepository extends JpaRepository<ProductOptionCombination, Integer> {
    void deleteByProductId(Integer productId);
    
    // 삭제되지 않은 조합만 조회
    @Query("SELECT poc FROM ProductOptionCombination poc WHERE poc.productId = :productId AND poc.isDeleted = 0")
    List<ProductOptionCombination> findByProductIdAndIsDeleted(@Param("productId") Integer productId, @Param("isDeleted") Integer isDeleted);

    // 위 메서드를 간단하게 수정
    @Query("SELECT poc FROM ProductOptionCombination poc WHERE poc.productId = :productId AND poc.isDeleted = 0")
    List<ProductOptionCombination> findActiveByProductId(@Param("productId") Integer productId);
}