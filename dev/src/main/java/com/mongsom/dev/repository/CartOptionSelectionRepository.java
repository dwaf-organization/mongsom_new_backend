package com.mongsom.dev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mongsom.dev.entity.CartOptionSelection;

@Repository
public interface CartOptionSelectionRepository extends JpaRepository<CartOptionSelection, Integer> {
    
    // 특정 장바구니 항목의 옵션 선택들 조회
    @Query("SELECT cos FROM CartOptionSelection cos WHERE cos.cartId = :cartId ORDER BY cos.optionTypeId")
    List<CartOptionSelection> findByCartIdOrderByOptionTypeId(@Param("cartId") Integer cartId);
    
    // 특정 장바구니 항목의 옵션 선택들 조회 (옵션 타입/값 정보 포함)
    @Query("SELECT cos FROM CartOptionSelection cos " +
           "JOIN FETCH cos.optionType " +
           "JOIN FETCH cos.optionValue " +
           "WHERE cos.cartId = :cartId " +
           "ORDER BY cos.optionType.sortOrder")
    List<CartOptionSelection> findByCartIdWithDetails(@Param("cartId") Integer cartId);
    
    // 여러 장바구니 항목의 옵션 선택들 조회
    @Query("SELECT cos FROM CartOptionSelection cos WHERE cos.cartId IN :cartIds ORDER BY cos.cartId, cos.optionTypeId")
    List<CartOptionSelection> findByCartIdIn(@Param("cartIds") List<Integer> cartIds);
    
    // 특정 장바구니의 옵션 개수 조회
    @Query("SELECT COUNT(cos) FROM CartOptionSelection cos WHERE cos.cartId = :cartId")
    Integer countByCartId(@Param("cartId") Integer cartId);
    
    // 특정 장바구니의 모든 옵션 선택 삭제
    @Modifying
    @Query("DELETE FROM CartOptionSelection cos WHERE cos.cartId = :cartId")
    int deleteByCartId(@Param("cartId") Integer cartId);
    
    // 특정 옵션 타입의 선택 삭제
    @Modifying
    @Query("DELETE FROM CartOptionSelection cos WHERE cos.cartId = :cartId AND cos.optionTypeId = :optionTypeId")
    int deleteByCartIdAndOptionTypeId(@Param("cartId") Integer cartId, @Param("optionTypeId") Integer optionTypeId);
    
    // 특정 옵션 값의 선택 삭제
    @Modifying
    @Query("DELETE FROM CartOptionSelection cos WHERE cos.cartId = :cartId AND cos.optionValueId = :optionValueId")
    int deleteByCartIdAndOptionValueId(@Param("cartId") Integer cartId, @Param("optionValueId") Integer optionValueId);
}