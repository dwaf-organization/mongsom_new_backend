package com.mongsom.dev.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.mongsom.dev.entity.OrderDetail;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    
    // 특정 주문의 상세 목록 조회
    @Query("SELECT od FROM OrderDetail od WHERE od.orderId = :orderId ORDER BY od.createdAt")
    List<OrderDetail> findByOrderIdOrderByCreatedAt(@Param("orderId") Integer orderId);
    
    // 리뷰 작성 가능한 주문 상세 조회 (배송완료 + 리뷰 미작성) - 네이티브 쿼리로 변경
    @Query(value = "SELECT od.* FROM order_detail od " +
           "JOIN order_item oi ON od.order_id = oi.order_id " +
           "WHERE od.user_code = :userCode " +
           "AND oi.delivery_status = '배송완료' " +
           "AND od.review_status = 0 " +
           "ORDER BY od.created_at DESC", nativeQuery = true)
    List<OrderDetail> findReviewableOrderDetails(@Param("userCode") Long userCode);
    
    // 네이티브 쿼리만 사용 - JPA 연관관계 무시
    @Query(value = "SELECT order_detail_id, order_id, user_code, opt_id, product_id, quantity, price, review_status, order_status, created_at, updated_at " +
                   "FROM order_detail WHERE order_id = :orderId ORDER BY order_detail_id", nativeQuery = true)
    List<Object[]> findOrderDetailsByOrderId(@Param("orderId") Integer orderId);
    
    // 주문 ID로 상품 상세 정보 조회 (네이티브 쿼리 사용)
    @Query(value = "SELECT * FROM order_detail WHERE order_id = :orderId ORDER BY order_detail_id", nativeQuery = true)
    List<OrderDetail> findByOrderIdWithProduct(@Param("orderId") Integer orderId);
    
    // 주문상세페이지용 - quantity, price, order_status 포함
    @Query(value = "SELECT od.order_detail_id, od.order_id, od.user_code, od.opt_id, od.product_id, " +
            "od.quantity, od.price, od.order_status, ci.change_status " +
            "FROM order_detail od " +
            "LEFT JOIN change_item ci ON od.order_detail_id = ci.order_detail_id " +
            "WHERE od.order_id = :orderId " +
            "ORDER BY od.order_detail_id", 
    nativeQuery = true)
List<Object[]> findOrderDetailItemsByOrderId(@Param("orderId") Integer orderId);
    
    // 리뷰 작성 가능 상품 조회 (페이징) - 배송완료 상품 중 리뷰 미작성 상품 (review_status = 0)
    @Query(value = "SELECT od.order_detail_id, od.opt_id, od.product_id, od.review_status, " +
                   "po.opt_name, p.name as product_name, oi.payment_at " +
                   "FROM order_detail od " +
                   "JOIN order_item oi ON od.order_id = oi.order_id " +
                   "JOIN product p ON od.product_id = p.product_id " +
                   "LEFT JOIN product_option po ON od.opt_id = po.opt_id AND od.product_id = po.product_id " +
                   "WHERE od.user_code = :userCode " +
                   "AND oi.delivery_status = '배송완료' " +
                   "AND od.review_status = 0 " +
                   "ORDER BY oi.payment_at DESC " +
                   "LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Object[]> findReviewableProducts(@Param("userCode") Long userCode, 
                                         @Param("limit") Integer limit, 
                                         @Param("offset") Integer offset);
    
    // 리뷰 작성 가능 상품 총 개수 (review_status = 0)
    @Query(value = "SELECT COUNT(*) " +
                   "FROM order_detail od " +
                   "JOIN order_item oi ON od.order_id = oi.order_id " +
                   "WHERE od.user_code = :userCode " +
                   "AND oi.delivery_status = '배송완료' " +
                   "AND od.review_status = 0", nativeQuery = true)
    Integer countReviewableProducts(@Param("userCode") Long userCode);
    
    // 작성한 리뷰 조회 (페이징) - review_status = 1인 항목만
    @Query(value = "SELECT od.order_detail_id, od.opt_id, od.product_id, od.review_status, " +
                   "po.opt_name, p.name as product_name, " +
                   "ur.review_id, ur.review_rating, ur.review_content, ur.created_at " +
                   "FROM order_detail od " +
                   "JOIN product p ON od.product_id = p.product_id " +
                   "LEFT JOIN product_option po ON od.opt_id = po.opt_id AND od.product_id = po.product_id " +
                   "JOIN user_review ur ON od.user_code = ur.user_code AND od.order_detail_id = ur.order_detail_id " +
                   "WHERE od.user_code = :userCode " +
                   "AND od.review_status = 1 " +
                   "ORDER BY ur.review_id DESC " +
                   "LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Object[]> findWrittenReviews(@Param("userCode") Long userCode, 
                                     @Param("limit") Integer limit, 
                                     @Param("offset") Integer offset);
    
    // 작성한 리뷰 총 개수 (review_status = 1)
    @Query(value = "SELECT COUNT(*) " +
                   "FROM order_detail od " +
                   "JOIN user_review ur ON od.user_code = ur.user_code AND od.order_detail_id = ur.order_detail_id " +
                   "WHERE od.user_code = :userCode " +
                   "AND od.review_status = 1", nativeQuery = true)
    Integer countWrittenReviews(@Param("userCode") Long userCode);
    
    // order_detail의 review_status 업데이트
    @Modifying
    @Query("UPDATE OrderDetail od SET od.reviewStatus = 1 WHERE od.orderDetailId = :orderDetailId")
    int updateReviewStatus(@Param("orderDetailId") Integer orderDetailId);
    
    // order_detail의 review_status를 0으로 업데이트 (리뷰 삭제 시 사용)
    @Modifying
    @Query("UPDATE OrderDetail od SET od.reviewStatus = 1 WHERE od.orderDetailId = :orderDetailId")
    int updateReviewStatusToZero(@Param("orderDetailId") Integer orderDetailId);
    
    // 관리자 주문 조회용 - orderId로 주문 상세 목록 조회
    List<OrderDetail> findByOrderId(Integer orderId);
    
    // 상품별 주문 개수 조회 (정상 주문만)
    long countByProductIdAndOrderStatus(Integer productId, Integer orderStatus);
    
    // 상품별 총 주문 수량 조회
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od WHERE od.productId = :productId AND od.orderStatus = 0")
    Long getTotalQuantityByProductId(@Param("productId") Integer productId);
    
    /**
     * 작성 가능한 리뷰 조회 (review_status = 0, 배송완료)
     */
    @Query("SELECT od FROM OrderDetail od " +
           "JOIN od.orderItem oi " +
           "WHERE oi.userCode = :userCode " +
           "AND od.reviewStatus = 0 " +
           "AND oi.deliveryStatus = '배송완료' " +
           "ORDER BY oi.paymentAt DESC")
    Page<OrderDetail> findReviewableOrderDetails(@Param("userCode") Long userCode, Pageable pageable);
    
    /**
     * 작성된 리뷰 조회 (review_status = 1, 관리자 임의숨김 제외)
     */
    @Query("SELECT od FROM OrderDetail od " +
           "JOIN od.orderItem oi " +
           "LEFT JOIN UserReview ur ON ur.orderDetailId = od.orderDetailId " +
           "WHERE oi.userCode = :userCode " +
           "AND od.reviewStatus = 1 " +
           "AND (ur.adminHidden IS NULL OR ur.adminHidden = 0) " +
           "ORDER BY oi.paymentAt DESC")
    Page<OrderDetail> findWrittenReviews(@Param("userCode") Long userCode, Pageable pageable);
    
    /**
     * 작성된 리뷰 조회 - 임의숨김 조건 제외 버전 (필요시)
     */
    @Query("SELECT od FROM OrderDetail od " +
           "JOIN od.orderItem oi " +
           "WHERE oi.userCode = :userCode " +
           "AND od.reviewStatus = 1 " +
           "ORDER BY oi.paymentAt DESC")
    Page<OrderDetail> findWrittenReviewsAll(@Param("userCode") Long userCode, Pageable pageable);
    
    /**
     * 특정 사용자의 전체 주문 상세 조회
     */
    @Query("SELECT od FROM OrderDetail od " +
           "JOIN od.orderItem oi " +
           "WHERE oi.userCode = :userCode " +
           "ORDER BY oi.createdAt DESC")
    Page<OrderDetail> findByUserCode(@Param("userCode") Long userCode, Pageable pageable);
    
    /**
     * 특정 상품의 리뷰 작성된 주문 상세 조회
     */
    @Query("SELECT od FROM OrderDetail od " +
           "JOIN od.orderItem oi " +
           "WHERE od.productId = :productId " +
           "AND od.reviewStatus = 1 " +
           "ORDER BY oi.paymentAt DESC")
    Page<OrderDetail> findReviewedOrderDetailsByProduct(@Param("productId") Integer productId, Pageable pageable);
    
    /**
     * 리뷰 통계용: 사용자별 리뷰 작성률 조회
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN od.reviewStatus = 1 THEN 1 END) as writtenCount, " +
           "COUNT(CASE WHEN od.reviewStatus = 0 AND oi.deliveryStatus = '배송완료' THEN 1 END) as writableCount " +
           "FROM OrderDetail od " +
           "JOIN od.orderItem oi " +
           "WHERE oi.userCode = :userCode")
    Object[] findReviewStatsByUser(@Param("userCode") Long userCode);
    
}