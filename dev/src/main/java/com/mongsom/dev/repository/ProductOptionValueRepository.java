package com.mongsom.dev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mongsom.dev.entity.ProductOptionValue;

@Repository
public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Integer> {
    
    // 옵션 타입의 값들 조회 (정렬 순서대로)
	@Query("SELECT pov FROM ProductOptionValue pov WHERE pov.optionTypeId = :optionTypeId AND pov.isDeleted = 0 ORDER BY pov.sortOrder, pov.optionValueId")
	List<ProductOptionValue> findByOptionTypeIdOrderBySortOrder(@Param("optionTypeId") Integer optionTypeId);

	@Query("SELECT pov FROM ProductOptionValue pov WHERE pov.optionTypeId = :optionTypeId AND pov.stockStatus = 1 AND pov.isDeleted = 0 ORDER BY pov.sortOrder")
	List<ProductOptionValue> findAvailableByOptionTypeId(@Param("optionTypeId") Integer optionTypeId);
    
    // 품절된 옵션 값들 조회
    @Query("SELECT pov FROM ProductOptionValue pov WHERE pov.optionTypeId = :optionTypeId AND pov.stockStatus = 0 ORDER BY pov.sortOrder")
    List<ProductOptionValue> findOutOfStockByOptionTypeId(@Param("optionTypeId") Integer optionTypeId);
    
    // 옵션 값명으로 조회 (같은 옵션 타입 내에서)
    @Query("SELECT pov FROM ProductOptionValue pov WHERE pov.optionTypeId = :optionTypeId AND pov.valueName = :valueName")
    ProductOptionValue findByOptionTypeIdAndValueName(@Param("optionTypeId") Integer optionTypeId, @Param("valueName") String valueName);
    
    // 옵션 타입의 값 개수 조회
    @Query("SELECT COUNT(pov) FROM ProductOptionValue pov WHERE pov.optionTypeId = :optionTypeId")
    Integer countByOptionTypeId(@Param("optionTypeId") Integer optionTypeId);
    
    // 가격 조정이 있는 옵션 값들 조회
    @Query("SELECT pov FROM ProductOptionValue pov WHERE pov.optionTypeId = :optionTypeId AND pov.priceAdjustment != 0 ORDER BY pov.sortOrder")
    List<ProductOptionValue> findPriceAdjustedByOptionTypeId(@Param("optionTypeId") Integer optionTypeId);
    
    // 특정 상품의 모든 옵션 값들 조회 (상품 ID를 통한 조인)
    @Query("SELECT pov FROM ProductOptionValue pov " +
    	       "JOIN ProductOptionType pot ON pov.optionTypeId = pot.optionTypeId " +
    	       "WHERE pot.productId = :productId AND pov.isDeleted = 0 AND pot.isDeleted = 0 " +
    	       "ORDER BY pot.sortOrder, pov.sortOrder")
    	List<ProductOptionValue> findAllByProductId(@Param("productId") Integer productId);
    
    // 여러 옵션 값 ID로 조회 (장바구니, 주문에서 사용)
    @Query("SELECT pov FROM ProductOptionValue pov WHERE pov.optionValueId IN :optionValueIds")
    List<ProductOptionValue> findByOptionValueIdIn(@Param("optionValueIds") List<Integer> optionValueIds);
    
    @Query("SELECT pov FROM ProductOptionValue pov WHERE pov.optionTypeId = :optionTypeId AND pov.isDeleted = 0")
    List<ProductOptionValue> findByOptionTypeId(@Param("optionTypeId") Integer optionTypeId);

    // 삭제된 것 포함해서 조회하는 메서드도 추가
    @Query("SELECT pov FROM ProductOptionValue pov WHERE pov.optionTypeId = :optionTypeId")
    List<ProductOptionValue> findByOptionTypeIdIncludingDeleted(@Param("optionTypeId") Integer optionTypeId);
}