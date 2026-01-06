package com.mongsom.dev.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.payment.reqDto.PaymentConfirmReqDto;
import com.mongsom.dev.entity.OrderItem;
import com.mongsom.dev.entity.Payments;
import com.mongsom.dev.repository.CartRepository;
import com.mongsom.dev.repository.OrderItemRepository;
import com.mongsom.dev.repository.PaymentsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final OrderItemRepository orderItemRepository;
    private final PaymentsRepository paymentsRepository;
    private final CartRepository cartRepository;
    
    @Value("${toss.secret-key}")
    private String tossSecretKey;
    
    // 토스페이먼츠 카드사 코드 매핑 테이블
    private static final Map<String, String> CARD_ISSUER_MAP = new HashMap<>();
    
    static {
        // 카드사 코드 → 한글명 매핑
        CARD_ISSUER_MAP.put("3K", "기업BC");
        CARD_ISSUER_MAP.put("46", "광주은행");
        CARD_ISSUER_MAP.put("71", "롯데카드");
        CARD_ISSUER_MAP.put("30", "산업은행");
        CARD_ISSUER_MAP.put("31", "BC카드");
        CARD_ISSUER_MAP.put("51", "삼성카드");
        CARD_ISSUER_MAP.put("38", "새마을금고");
        CARD_ISSUER_MAP.put("41", "신한카드");
        CARD_ISSUER_MAP.put("62", "신협");
        CARD_ISSUER_MAP.put("36", "씨티카드");
        CARD_ISSUER_MAP.put("33", "우리BC카드");
        CARD_ISSUER_MAP.put("W1", "우리카드");
        CARD_ISSUER_MAP.put("37", "우체국예금보험");
        CARD_ISSUER_MAP.put("39", "저축은행중앙회");
        CARD_ISSUER_MAP.put("35", "전북은행");
        CARD_ISSUER_MAP.put("42", "제주은행");
        CARD_ISSUER_MAP.put("15", "카카오뱅크");
        CARD_ISSUER_MAP.put("3A", "케이뱅크");
        CARD_ISSUER_MAP.put("24", "토스뱅크");
        CARD_ISSUER_MAP.put("21", "하나카드");
        CARD_ISSUER_MAP.put("61", "현대카드");
        CARD_ISSUER_MAP.put("11", "국민카드");
        CARD_ISSUER_MAP.put("91", "농협카드");
        CARD_ISSUER_MAP.put("34", "수협은행");
    }
    
    /**
     * 토스페이먼츠 결제 승인
     */
    @Transactional
    public RespDto<String> confirmPayment(PaymentConfirmReqDto reqDto) {
        try {
            log.info("=== 토스페이먼츠 결제 승인 요청 시작 ===");
            log.info("paymentKey: {}, orderId: {}, amount: {}",
                    reqDto.getPaymentKey(), reqDto.getOrderId(), reqDto.getAmount());

            // 1. 토스페이먼츠 API 엔드포인트
            String url = "https://api.tosspayments.com/v1/payments/confirm";

            // 2. 인증 헤더 생성 (Secret Key를 Base64 인코딩)
            String encodedAuth = Base64.getEncoder()
                    .encodeToString((tossSecretKey + ":").getBytes());

            // 3. 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + encodedAuth);

            // 4. 요청 바디 생성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("paymentKey", reqDto.getPaymentKey());
            requestBody.put("orderId", reqDto.getOrderId());
            requestBody.put("amount", reqDto.getAmount());

            // 5. HTTP 요청 생성
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 6. 토스페이먼츠 API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            // 7. 응답 로깅
            log.info("=== 토스페이먼츠 응답 ===");
            log.info("Status Code: {}", response.getStatusCode());
            log.info("Response Body: {}", objectMapper.writeValueAsString(response.getBody()));

            Map<String, Object> responseBody = response.getBody();

            // 8. 응답이 성공이면 DB 업데이트 진행
            if (responseBody != null && "DONE".equals(responseBody.get("status"))) {
                log.info("=== DB 업데이트 시작 ===");
                
                String orderNum = (String) responseBody.get("orderId");  // mongsom_100023
                String method = (String) responseBody.get("method");  // "카드"
                Integer totalAmount = (Integer) responseBody.get("totalAmount");  // 68000
                String paymentKey = (String) responseBody.get("paymentKey");  // "tviva20250929143411SGeO6"
                
                log.info("orderNum: {}, method: {}, totalAmount: {}, paymentKey: {}", 
                        orderNum, method, totalAmount, paymentKey);
                
                // 8-1. orderNum으로 주문 조회
                Optional<OrderItem> orderItemOpt = orderItemRepository.findByOrderNum(orderNum);
                
                if (orderItemOpt.isEmpty()) {
                    log.error("주문을 찾을 수 없음 - orderNum: {}", orderNum);
                    return RespDto.<String>builder()
                            .code(-1)
                            .data("주문을 찾을 수 없습니다.")
                            .build();
                }
                
                OrderItem orderItem = orderItemOpt.get();
                Integer orderId = orderItem.getOrderId();
                
                log.info("주문 조회 성공 - orderId: {}, orderNum: {}", orderId, orderNum);
                
                // 8-2. OrderItem 업데이트
                orderItem.setDeliveryStatus("결제완료");
                orderItem.setPaymentAt(LocalDateTime.now());
                orderItemRepository.save(orderItem);
                
                log.info("OrderItem 업데이트 완료 - orderId: {}, deliveryStatus: 결제완료", orderId);
                
                // 8-3. Payments 조회
                List<Payments> paymentList = paymentsRepository.findByOrderId(orderId);
                
                if (paymentList == null || paymentList.isEmpty()) {
                    log.error("결제 정보를 찾을 수 없음 - orderId: {}", orderId);
                    return RespDto.<String>builder()
                            .code(-1)
                            .data("결제 정보를 찾을 수 없습니다.")
                            .build();
                }
                
                // 8-4. Payments 업데이트 (리스트의 모든 결제 정보 업데이트)
                for (Payments payment : paymentList) {
                    // 카드사 정보 추출
                    String paymentMethodInfo = extractPaymentMethodInfo(responseBody);
                    
                    payment.setPaymentMethod(paymentMethodInfo);
                    payment.setPaymentAmount(totalAmount);
                    payment.setPaymentStatus("결제완료");
                    payment.setPaymentKey(paymentKey);
                    paymentsRepository.save(payment);
                    
                    log.info("Payments 업데이트 완료 - paymentId: {}, orderId: {}, method: {}, amount: {}, status: COMPLETED", 
                            payment.getPaymentId(), orderId, paymentMethodInfo, totalAmount);
                }
                
                // 8-5. 결제 완료 시 장바구니 삭제
                int deletedCartCount = cartRepository.deleteByUserCode(reqDto.getUserCode());
                log.info("장바구니 삭제 완료 - userCode: {}, 삭제된 항목 수: {}", 
                        reqDto.getUserCode(), deletedCartCount);
                
                log.info("=== DB 업데이트 완료 ===");
                
                return RespDto.<String>builder()
                        .code(1)
                        .data("결제가 승인되었습니다.")
                        .build();
                
            } else {
                log.warn("결제 상태가 DONE이 아님 - status: {}", responseBody != null ? responseBody.get("status") : "null");
                return RespDto.<String>builder()
                        .code(-1)
                        .data("결제 승인 실패")
                        .build();
            }

        } catch (HttpClientErrorException e) {
            log.error("토스페이먼츠 API 호출 실패", e);
            log.error("Status Code: {}", e.getStatusCode());
            log.error("Response Body: {}", e.getResponseBodyAsString());
            
            return RespDto.<String>builder()
                    .code(-1)
                    .data("토스페이먼츠 API 호출 실패: " + e.getStatusCode())
                    .build();
                    
        } catch (Exception e) {
            log.error("결제 승인 처리 중 오류 발생", e);
            
            return RespDto.<String>builder()
                    .code(-1)
                    .data("결제 승인 처리 중 오류 발생: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 토스페이먼츠 결제 조회 (paymentKey 사용)
     */
    public Map<String, Object> getPaymentByKey(String paymentKey) {
        try {
            log.info("=== 토스페이먼츠 결제 조회 요청 시작 ===");
            log.info("paymentKey: {}", paymentKey);
            
            // 1. 토스페이먼츠 API 엔드포인트
            String url = "https://api.tosspayments.com/v1/payments/" + paymentKey;
            
            // 2. 인증 헤더 생성 (Secret Key를 Base64 인코딩)
            String encodedAuth = Base64.getEncoder()
                    .encodeToString((tossSecretKey + ":").getBytes());
            
            // 3. 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);
            
            // 4. HTTP 요청 생성
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            // 5. 토스페이먼츠 API 호출 (GET)
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, 
                    HttpMethod.GET, 
                    request, 
                    Map.class
            );
            
            // 6. 응답 로깅
            log.info("=== 토스페이먼츠 결제 조회 응답 ===");
            log.info("Status Code: {}", response.getStatusCode());
            log.info("Response Body: {}", objectMapper.writeValueAsString(response.getBody()));
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("토스페이먼츠 결제 조회 실패", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", e.getMessage());
            
            return errorResponse;
        }
    }
    
    /**
     * 토스페이먼츠 응답에서 결제수단 정보 추출 (카드사명 매핑 포함)
     */
    private String extractPaymentMethodInfo(Map<String, Object> responseBody) {
        try {
            String method = (String) responseBody.get("method");
            log.info("결제 방법: {}", method);
            
            if ("카드".equals(method)) {
                // 카드 결제인 경우 issuerCode를 통해 카드사명 추출
                Map<String, Object> cardInfo = (Map<String, Object>) responseBody.get("card");
                if (cardInfo != null) {
                    String issuerCode = (String) cardInfo.get("issuerCode");
                    log.info("카드 발급사 코드: {}", issuerCode);
                    
                    if (issuerCode != null && !issuerCode.isEmpty()) {
                        // issuerCode를 카드사명으로 매핑
                        String cardCompanyName = CARD_ISSUER_MAP.get(issuerCode);
                        
                        if (cardCompanyName != null) {
                            log.info("매핑된 카드사명: {}", cardCompanyName);
                            return cardCompanyName; // 예: "신한카드", "토스뱅크", "삼성카드"
                        } else {
                            log.warn("알 수 없는 카드사 코드: {}", issuerCode);
                            // 알 수 없는 코드인 경우 기본값 + 코드 표시
                            return "카드(" + issuerCode + ")";
                        }
                    }
                }
                
                // issuerCode가 없거나 카드 정보가 없는 경우
                log.warn("카드 발급사 코드를 찾을 수 없음");
                return "카드";
                
            } else if ("간편결제".equals(method)) {
                // 간편결제인 경우 제공업체 정보 추출 (기존 로직 유지)
                Map<String, Object> easyPayInfo = (Map<String, Object>) responseBody.get("easyPay");
                if (easyPayInfo != null) {
                    String provider = (String) easyPayInfo.get("provider");
                    if (provider != null && !provider.isEmpty()) {
                        log.info("간편결제 제공업체: {}", provider);
                        return provider; // 예: "토스페이", "카카오페이"
                    }
                }
                return "간편결제";
                
            } else {
                // 가상계좌, 계좌이체 등 기타 결제수단
                log.info("기타 결제수단: {}", method);
                return method;
            }
            
        } catch (Exception e) {
            log.error("결제수단 정보 추출 실패: {}", e.getMessage(), e);
            // 오류 발생 시 원본 method 반환
            return (String) responseBody.get("method");
        }
    }
    
    /**
     * 카드사 코드 목록 조회 (디버깅/관리용)
     */
    public Map<String, String> getCardIssuerMap() {
        return new HashMap<>(CARD_ISSUER_MAP);
    }
    
    /**
     * 특정 카드사 코드로 카드사명 조회 (유틸리티 메서드)
     */
    public String getCardCompanyName(String issuerCode) {
        if (issuerCode == null || issuerCode.isEmpty()) {
            return "알 수 없음";
        }
        
        String cardCompanyName = CARD_ISSUER_MAP.get(issuerCode);
        return cardCompanyName != null ? cardCompanyName : "카드(" + issuerCode + ")";
    }
}