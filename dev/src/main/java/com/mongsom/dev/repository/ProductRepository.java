package com.mongsom.dev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mongsom.dev.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // 상품명으로 조회
    Optional<Product> findByName(String name);
    
    // 상품명 중복 체크
    boolean existsByName(String name);
    
    // 프리미엄 상품만 조회 (리스트)
    Page<Product> findByPremiumAndDeleteStatus(Integer premium, Integer deleteStatus, Pageable pageable);
    
    // 삭제되지 않은 상품만 조회 (페이징)
    Page<Product> findByDeleteStatusOrderByCreatedAtDesc(Integer deleteStatus, Pageable pageable);
    
    // 또는 더 명확한 메서드명
    Page<Product> findByDeleteStatus(Integer deleteStatus, Pageable pageable);
    
    // 인기순 조회 (주문 횟수 기준, 페이징 지원) - 삭제되지 않은 상품만
    @Query(value = "SELECT p.* FROM product p " +
                   "LEFT JOIN (SELECT od.product_id, COUNT(*) as order_count " +
                   "           FROM order_detail od " +
                   "           WHERE od.order_status = 0 " +
                   "           GROUP BY od.product_id) o ON p.product_id = o.product_id " +
                   "WHERE p.delete_status = 0 " +
                   "ORDER BY COALESCE(o.order_count, 0) DESC, p.product_id DESC",
           countQuery = "SELECT COUNT(p.product_id) FROM product p WHERE p.delete_status = 0",
           nativeQuery = true)
    Page<Product> findAllOrderByPopularityDesc(Pageable pageable);
    
    // 상품명으로 검색 (LIKE 검색)
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% AND p.deleteStatus = 0")
    List<Product> findByNameContaining(@Param("keyword") String keyword);
    
    // 생성일 기준 최신 상품 조회
    @Query("SELECT p FROM Product p WHERE p.deleteStatus = 0 ORDER BY p.createdAt DESC")
    List<Product> findAllOrderByCreatedAtDesc();
    
    // 리뷰가 많은 순으로 상품 조회 (네이티브 쿼리 사용)
    @Query(value = "SELECT p.* FROM product p " +
                   "LEFT JOIN (" +
                   "    SELECT product_id, COUNT(*) as review_count " +
                   "    FROM user_review " +
                   "    GROUP BY product_id" +
                   ") r ON p.product_id = r.product_id " +
                   "WHERE p.delete_status = 0 " +
                   "ORDER BY COALESCE(r.review_count, 0) DESC, p.product_id DESC",
           countQuery = "SELECT COUNT(*) FROM product WHERE delete_status = 0",
           nativeQuery = true)
    Page<Product> findAllOrderByReviewCountDesc(Pageable pageable);
    
    // 상품과 옵션 타입을 함께 조회 (수정됨)
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.optionTypes " +
           "WHERE p.productId = :productId")
    Optional<Product> findByIdWithOptions(@Param("productId") Integer productId);
    
    // 상품과 이미지를 함께 조회
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.productImages " +
           "WHERE p.productId = :productId")
    Optional<Product> findByIdWithImages(@Param("productId") Integer productId);
    
    // 상품 기본 정보만 조회 (MultipleBagFetchException 방지용)
    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> findByIdOnly(@Param("productId") Integer productId);
    
    // 기본 상품 조회
    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> findByIdWithoutFetch(@Param("productId") Integer productId);
    
    // 관리자페이지
    
    // 상품명으로 검색 (LIKE %name%)
    Page<Product> findByNameContainingAndDeleteStatus(String name, Integer deleteStatus, Pageable pageable);
    
    // 상품명 + 프리미엄 조건 모두 적용
    Page<Product> findByNameContainingAndPremiumAndDeleteStatus(String name, Integer premium, Integer deleteStatus, Pageable pageable);
    
    // 재고 상태별 조회
    @Query("SELECT p FROM Product p WHERE p.stockStatus = :stockStatus AND p.deleteStatus = 0")
    List<Product> findByStockStatus(@Param("stockStatus") Integer stockStatus);
    
    // 판매 가능한 상품 조회
    @Query("SELECT p FROM Product p WHERE p.isAvailable = 1 AND p.deleteStatus = 0 AND p.stockStatus > 0")
    List<Product> findAvailableProducts();
    
    // 관리자 상품 목록 조회 (검색 조건 포함)
    @Query(value = "SELECT p.*, " +
                   "       (SELECT pi.product_img_url FROM product_img pi WHERE pi.product_id = p.product_id ORDER BY pi.created_at LIMIT 1) as first_image_url, " +
                   "       (SELECT COUNT(*) FROM product_img pi WHERE pi.product_id = p.product_id) as image_count, " +
                   "       (SELECT COUNT(*) FROM product_option_type pot WHERE pot.product_id = p.product_id) as option_type_count, " +
                   "       (SELECT COUNT(*) FROM product_option_value pov JOIN product_option_type pot ON pov.option_type_id = pot.option_type_id WHERE pot.product_id = p.product_id) as total_option_value_count " +
                   "FROM product p " +
                   "WHERE (:name IS NULL OR p.name LIKE CONCAT('%', :name, '%')) " +
                   "AND (:premium = 2 OR p.premium = :premium) " +
                   "AND (:outOfStock = 0 OR (:outOfStock = 1 AND p.stock_status = 0)) " +
                   "AND (:paused = 0 OR (:paused = 1 AND p.is_available = 0)) " +
                   "AND p.delete_status = 0 " +
                   "ORDER BY p.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM product p " +
                       "WHERE (:name IS NULL OR p.name LIKE CONCAT('%', :name, '%')) " +
                       "AND (:premium = 2 OR p.premium = :premium) " +
                       "AND (:outOfStock = 0 OR (:outOfStock = 1 AND p.stock_status = 0)) " +
                       "AND (:paused = 0 OR (:paused = 1 AND p.is_available = 0)) " +
                       "AND p.delete_status = 0",
           nativeQuery = true)
    Page<Object[]> findProductsWithDetails(@Param("name") String name,
                                         @Param("premium") Integer premium,
                                         @Param("outOfStock") Integer outOfStock,
                                         @Param("paused") Integer paused,
                                         Pageable pageable);
    
    // 특정 상품의 옵션 타입명들 조회
    @Query("SELECT pot.typeName FROM ProductOptionType pot WHERE pot.productId = :productId ORDER BY pot.sortOrder")
    List<String> findOptionTypeNamesByProductId(@Param("productId") Integer productId);
}