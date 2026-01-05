package com.mongsom.dev.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mongsom.dev.entity.OrderDetail;
import com.mongsom.dev.entity.UserReview;

@Repository
public interface UserReviewRepository extends JpaRepository<UserReview, Integer> {
    
    // 상품의 리뷰 조회 (최신순, 페이징)
    @Query("SELECT ur FROM UserReview ur " +
           "JOIN FETCH ur.user u " +
           "WHERE ur.productId = :productId " +
           "ORDER BY ur.createdAt DESC")
    Page<UserReview> findByProductIdOrderByCreatedAtDesc(@Param("productId") Integer productId, Pageable pageable);
    
    // 상품의 리뷰 개수 조회
    @Query("SELECT COUNT(ur) FROM UserReview ur WHERE ur.productId = :productId")
    Long countByProductId(@Param("productId") Integer productId);
    
    // 사용자의 리뷰 조회
    @Query("SELECT ur FROM UserReview ur " +
           "JOIN FETCH ur.user u " +
           "WHERE ur.userCode = :userCode " +
           "ORDER BY ur.createdAt DESC")
    Page<UserReview> findByUserCodeOrderByCreatedAtDesc(@Param("userCode") Integer userCode, Pageable pageable);
    
    // 상품별 평균 평점 조회
    @Query("SELECT AVG(ur.reviewRating) FROM UserReview ur WHERE ur.productId = :productId")
    Double getAverageRatingByProductId(@Param("productId") Integer productId);
    
    
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
     * 작성된 리뷰 조회 (review_status = 1)
     */
    @Query("SELECT od FROM OrderDetail od " +
           "JOIN od.orderItem oi " +
           "WHERE oi.userCode = :userCode " +
           "AND od.reviewStatus = 1 " +
           "ORDER BY oi.paymentAt DESC")
    Page<OrderDetail> findWrittenReviews(@Param("userCode") Long userCode, Pageable pageable);
    

    /**
     * 사용자별 리뷰 조회 (숨김 처리된 것 제외)
     */
    @Query("SELECT ur FROM UserReview ur " +
           "WHERE ur.userCode = :userCode " +
           "AND ur.adminHidden = 0 " +
           "ORDER BY ur.createdAt DESC")
    Page<UserReview> findByUserCodeAndNotHidden(@Param("userCode") Long userCode, Pageable pageable);
    
    /**
     * 상품별 리뷰 조회 (숨김 처리된 것 제외)
     */
    @Query("SELECT ur FROM UserReview ur " +
           "WHERE ur.productId = :productId " +
           "AND ur.adminHidden = 0 " +
           "ORDER BY ur.createdAt DESC")
    Page<UserReview> findByProductIdAndNotHidden(@Param("productId") Integer productId, Pageable pageable);
    
    /**
     * 전체 리뷰 조회 (숨김 처리된 것 제외)
     */
    @Query("SELECT ur FROM UserReview ur " +
           "WHERE ur.adminHidden = 0 " +
           "ORDER BY ur.createdAt DESC")
    Page<UserReview> findAllNotHidden(Pageable pageable);
    
    /**
     * 관리자용: 전체 리뷰 조회 (숨김 포함)
     */
    Page<UserReview> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 관리자용: 숨김 상태별 리뷰 조회
     */
    @Query("SELECT ur FROM UserReview ur " +
           "WHERE ur.adminHidden = :adminHidden " +
           "ORDER BY ur.createdAt DESC")
    Page<UserReview> findByAdminHidden(@Param("adminHidden") Integer adminHidden, Pageable pageable);
    
    /**
     * 관리자용: 숨김 처리된 리뷰 목록
     */
    @Query("SELECT ur FROM UserReview ur " +
           "WHERE ur.adminHidden = 1 " +
           "ORDER BY ur.updatedAt DESC")
    Page<UserReview> findHiddenReviews(Pageable pageable);
    
    /**
     * 특정 주문 상세에 대한 리뷰 존재 확인 (숨김 제외)
     */
    @Query("SELECT COUNT(ur) > 0 FROM UserReview ur " +
           "WHERE ur.orderDetailId = :orderDetailId " +
           "AND ur.adminHidden = 0")
    boolean existsByOrderDetailIdAndNotHidden(@Param("orderDetailId") Integer orderDetailId);
    
    /**
     * 특정 주문 상세에 대한 리뷰 조회 (숨김 제외)
     */
    @Query("SELECT ur FROM UserReview ur " +
           "WHERE ur.orderDetailId = :orderDetailId " +
           "AND ur.adminHidden = 0")
    UserReview findByOrderDetailIdAndNotHidden(@Param("orderDetailId") Integer orderDetailId);
    
    /**
     * 사용자 및 주문 상세로 리뷰 조회 (권한 확인용)
     */
    @Query("SELECT ur FROM UserReview ur " +
           "WHERE ur.userCode = :userCode " +
           "AND ur.orderDetailId = :orderDetailId")
    UserReview findByUserCodeAndOrderDetailId(@Param("userCode") Long userCode, 
                                             @Param("orderDetailId") Integer orderDetailId);
    
    /**
     * 주문별 리뷰 조회
     */
    @Query("SELECT ur FROM UserReview ur " +
           "WHERE ur.orderId = :orderId " +
           "AND ur.adminHidden = 0 " +
           "ORDER BY ur.createdAt DESC")
    List<UserReview> findByOrderIdAndNotHidden(@Param("orderId") Integer orderId);
    
}