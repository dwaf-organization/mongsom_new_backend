package com.mongsom.dev.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "change_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeItem {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_id")
    private Integer changeId;
    
    @Column(name = "order_detail_id", nullable = false)
    private Integer orderDetailId;
    
    @Column(name = "order_id", nullable = false)
    private Integer orderId;
    
    @Column(name = "user_code", nullable = false)
    private Long userCode;
    
    @Column(name = "change_type", length = 20, nullable = false)
    private String changeType; // 교환, 반품
    
    @Column(name = "change_status", length = 30)
    private String changeStatus; // 교환신청, 교환승인, 교환거부, 교환배송중, 교환완료, 반품신청, 반품승인, 반품거부, 반품배송중, 반품완료
    
    @Column(name = "reason", length = 500)
    private String reason; // 교환반품 사유
    
    @Column(name = "refund_bank", length = 50)
    private String refundBank; // 반품시 은행명
    
    @Column(name = "refund_account", length = 100)
    private String refundAccount; // 반품시 계좌번호
    
    @Column(name = "admin_memo", columnDefinition = "text")
    private String adminMemo; // 관리자메모
    
    @Column(name = "requested_at")
    private LocalDateTime requestedAt; // 교환반품 요청일
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt; // 승인일자
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", insertable = false, updatable = false)
    private OrderDetail orderDetail;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private OrderItem orderItem;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", insertable = false, updatable = false)
    private User user;
    
    // 정적 팩토리 메서드
    public static ChangeItem createExchange(Integer orderDetailId, Integer orderId, Long userCode,
                                          String reason, String refundBank, String refundAccount) {
        return ChangeItem.builder()
                .orderDetailId(orderDetailId)
                .orderId(orderId)
                .userCode(userCode)
                .changeType("교환")
                .changeStatus("교환신청")
                .reason(reason)
                .refundBank(refundBank)
                .refundAccount(refundAccount)
                .requestedAt(LocalDateTime.now())
                .build();
    }
    
    public static ChangeItem createReturn(Integer orderDetailId, Integer orderId, Long userCode,
                                        String reason, String refundBank, String refundAccount) {
        return ChangeItem.builder()
                .orderDetailId(orderDetailId)
                .orderId(orderId)
                .userCode(userCode)
                .changeType("반품")
                .changeStatus("반품신청")
                .reason(reason)
                .refundBank(refundBank)
                .refundAccount(refundAccount)
                .requestedAt(LocalDateTime.now())
                .build();
    }
}