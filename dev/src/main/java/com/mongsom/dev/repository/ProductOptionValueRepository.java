package com.mongsom.dev.repository;

import java.util.List;
import java.util.Optional;

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
    
    /**
     * 특정 상품의 유효한 옵션값인지 확인
     * 옵션값이 해당 상품의 옵션 타입에 속하는지 검증
     */
    @Query("SELECT COUNT(ov) > 0 FROM ProductOptionValue ov " +
           "JOIN ProductOptionType ot ON ov.optionTypeId = ot.optionTypeId " +
           "WHERE ov.optionValueId = :optionValueId " +
           "AND ot.productId = :productId " +
           "AND (ov.isDeleted IS NULL OR ov.isDeleted = 0) " +
           "AND (ot.isDeleted IS NULL OR ot.isDeleted = 0)")
    boolean existsByOptionValueIdAndProductId(@Param("optionValueId") Integer optionValueId, 
                                             @Param("productId") Integer productId);

    /**
     * 옵션값 ID로 상품 ID 조회
     */
    @Query("SELECT ot.productId FROM ProductOptionValue ov " +
           "JOIN ProductOptionType ot ON ov.optionTypeId = ot.optionTypeId " +
           "WHERE ov.optionValueId = :optionValueId")
    Optional<Integer> findProductIdByOptionValueId(@Param("optionValueId") Integer optionValueId);

    /**
     * 옵션값과 함께 옵션 타입 정보도 조회
     */
    @Query("SELECT ov FROM ProductOptionValue ov " +
           "JOIN FETCH ov.optionType ot " +
           "WHERE ov.optionValueId = :optionValueId")
    Optional<ProductOptionValue> findByIdWithOptionType(@Param("optionValueId") Integer optionValueId);
    
}