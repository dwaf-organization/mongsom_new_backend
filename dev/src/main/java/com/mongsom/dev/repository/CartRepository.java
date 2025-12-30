package com.mongsom.dev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mongsom.dev.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    
    // 사용자별 장바구니 조회 (최신순)
    @Query("SELECT c FROM Cart c WHERE c.userCode = :userCode ORDER BY c.createdAt DESC")
    List<Cart> findByUserCodeOrderByCreatedAtDesc(@Param("userCode") Long userCode);
    
    // 사용자의 장바구니 조회 (상품 정보와 함께)
    @Query("SELECT c FROM Cart c " +
           "JOIN FETCH c.product p " +
           "WHERE c.userCode = :userCode " +
           "ORDER BY c.createdAt DESC")
    List<Cart> findByUserCodeWithProduct(@Param("userCode") Long userCode);
    
    // 사용자의 체크된 장바구니 아이템 조회 (상품 정보와 함께)
    @Query("SELECT c FROM Cart c " +
           "JOIN FETCH c.product p " +
           "WHERE c.userCode = :userCode AND c.checkStatus = 1 " +
           "ORDER BY c.createdAt DESC")
    List<Cart> findByUserCodeAndCheckStatusTrueWithProduct(@Param("userCode") Long userCode);
    
    // 사용자의 장바구니 개수 조회
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.userCode = :userCode")
    Long countByUserCode(@Param("userCode") Long userCode);
    
    // 특정 상품이 이미 장바구니에 있는지 확인 (옵션 조합 포함)
    @Query("SELECT c FROM Cart c WHERE c.userCode = :userCode AND c.productId = :productId " +
           "AND (:combinationId IS NULL AND c.combinationId IS NULL OR c.combinationId = :combinationId)")
    Optional<Cart> findByUserCodeAndProductIdAndCombinationId(
            @Param("userCode") Long userCode, 
            @Param("productId") Integer productId, 
            @Param("combinationId") Integer combinationId);
    
    // 옵션이 없는 상품 장바구니 조회
    @Query("SELECT c FROM Cart c WHERE c.userCode = :userCode AND c.productId = :productId AND c.combinationId IS NULL")
    Optional<Cart> findByUserCodeAndProductIdAndCombinationIdIsNull(
            @Param("userCode") Long userCode, 
            @Param("productId") Integer productId);
    
    // 사용자의 특정 상품 모든 장바구니 조회
    @Query("SELECT c FROM Cart c WHERE c.userCode = :userCode AND c.productId = :productId")
    List<Cart> findByUserCodeAndProductId(
            @Param("userCode") Long userCode, 
            @Param("productId") Integer productId);
    
    // 특정 상품의 모든 장바구니 항목 삭제
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.productId = :productId")
    int deleteByProductId(@Param("productId") Integer productId);
    
    // 사용자의 특정 상품 장바구니 삭제
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.userCode = :userCode AND c.productId = :productId")
    int deleteByUserCodeAndProductId(
            @Param("userCode") Long userCode, 
            @Param("productId") Integer productId);
    
    // 특정 장바구니 아이템 조회 (권한 확인용)
    @Query("SELECT c FROM Cart c WHERE c.cartId = :cartId AND c.userCode = :userCode")
    Optional<Cart> findByCartIdAndUserCode(
            @Param("cartId") Integer cartId, 
            @Param("userCode") Long userCode);
    
    // 사용자별 전체 체크 상태 변경
    @Modifying
    @Query("UPDATE Cart c SET c.checkStatus = :checkStatus WHERE c.userCode = :userCode")
    int updateAllCheckStatusByUserCode(@Param("userCode") Long userCode, 
                                      @Param("checkStatus") Integer checkStatus);
    
    // 회원탈퇴용 - userCode로 모든 장바구니 데이터 삭제
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.userCode = :userCode")
    int deleteByUserCode(@Param("userCode") Long userCode);
    
    // 옵션 조합별 삭제 (정확한 매칭)
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.userCode = :userCode AND c.productId = :productId AND c.combinationId = :combinationId")
    int deleteByUserCodeAndProductIdAndCombinationId(
            @Param("userCode") Long userCode,
            @Param("productId") Integer productId,
            @Param("combinationId") Integer combinationId);
    
    // 옵션 없는 상품 삭제
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.userCode = :userCode AND c.productId = :productId AND c.combinationId IS NULL")
    int deleteByUserCodeAndProductIdAndCombinationIdIsNull(
            @Param("userCode") Long userCode,
            @Param("productId") Integer productId);

    /**
     * 사용자별 장바구니 조회 (상품, 옵션조합 정보 포함)
     */
    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.product p " +
           "LEFT JOIN FETCH c.optionCombination oc " +
           "WHERE c.userCode = :userCode " +
           "ORDER BY c.createdAt DESC")
    List<Cart> findByUserCodeWithDetails(@Param("userCode") Long userCode);

    /**
     * 장바구니 수량 업데이트
     */
    @Modifying
    @Query("UPDATE Cart c SET c.quantity = :quantity WHERE c.userCode = :userCode AND c.productId = :productId AND c.combinationId = :combinationId")
    int updateQuantityByUserCodeAndProductIdAndCombinationId(@Param("userCode") Long userCode,
                                                             @Param("productId") Integer productId, 
                                                             @Param("combinationId") Integer combinationId,
                                                             @Param("quantity") Integer quantity);

    /**
     * 옵션 없는 상품 수량 업데이트
     */
    @Modifying
    @Query("UPDATE Cart c SET c.quantity = :quantity WHERE c.userCode = :userCode AND c.productId = :productId AND c.combinationId IS NULL")
    int updateQuantityByUserCodeAndProductIdAndCombinationIdIsNull(@Param("userCode") Long userCode,
                                                                   @Param("productId") Integer productId,
                                                                   @Param("quantity") Integer quantity);
}