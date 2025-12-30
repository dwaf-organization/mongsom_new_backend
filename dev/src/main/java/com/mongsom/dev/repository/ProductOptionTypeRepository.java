package com.mongsom.dev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mongsom.dev.entity.ProductOptionType;

@Repository
public interface ProductOptionTypeRepository extends JpaRepository<ProductOptionType, Integer> {
    
    // 상품의 옵션 타입들 조회 (정렬 순서대로)
	@Query("SELECT pot FROM ProductOptionType pot WHERE pot.productId = :productId AND pot.isDeleted = 0 ORDER BY pot.sortOrder, pot.optionTypeId")
	List<ProductOptionType> findByProductIdOrderBySortOrder(@Param("productId") Integer productId);

	@Query("SELECT pot FROM ProductOptionType pot WHERE pot.productId = :productId AND pot.isRequired = 1 AND pot.isDeleted = 0 ORDER BY pot.sortOrder")
	List<ProductOptionType> findRequiredOptionsByProductId(@Param("productId") Integer productId);
    
    // 상품의 선택 옵션 타입들 조회
    @Query("SELECT pot FROM ProductOptionType pot WHERE pot.productId = :productId AND pot.isRequired = 0 ORDER BY pot.sortOrder")
    List<ProductOptionType> findOptionalOptionsByProductId(@Param("productId") Integer productId);
    
    // 옵션 타입과 값들 함께 조회
    @Query("SELECT DISTINCT pot FROM ProductOptionType pot " +
    	       "LEFT JOIN FETCH pot.optionValues pov " +
    	       "WHERE pot.productId = :productId AND pot.isDeleted = 0 AND (pov.isDeleted = 0 OR pov.isDeleted IS NULL) " +
    	       "ORDER BY pot.sortOrder")
    	List<ProductOptionType> findByProductIdWithValues(@Param("productId") Integer productId);
    
    // 상품의 옵션 타입 개수 조회
    @Query("SELECT COUNT(pot) FROM ProductOptionType pot WHERE pot.productId = :productId")
    Integer countByProductId(@Param("productId") Integer productId);
    
    // 옵션 타입명으로 조회 (같은 상품 내에서)
    @Query("SELECT pot FROM ProductOptionType pot WHERE pot.productId = :productId AND pot.typeName = :typeName")
    ProductOptionType findByProductIdAndTypeName(@Param("productId") Integer productId, @Param("typeName") String typeName);
    
    /**
     * 상품별 옵션 타입 조회 (삭제되지 않은 것만, 정렬 적용)
     */
    @Query("SELECT ot FROM ProductOptionType ot " +
           "WHERE ot.productId = :productId " +
           "AND (ot.isDeleted IS NULL OR ot.isDeleted = 0) " +
           "ORDER BY ot.sortOrder ASC")
    List<ProductOptionType> findByProductIdAndNotDeleted(@Param("productId") Integer productId);
    
}