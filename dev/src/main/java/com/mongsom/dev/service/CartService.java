package com.mongsom.dev.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.cart.reqDto.CartAddReqDto;
import com.mongsom.dev.dto.cart.reqDto.CartUpdateQuantityReqDto;
import com.mongsom.dev.dto.cart.respDto.CartRespDto;
import com.mongsom.dev.entity.Cart;
import com.mongsom.dev.entity.Product;
import com.mongsom.dev.entity.ProductImg;
import com.mongsom.dev.entity.User;
import com.mongsom.dev.repository.CartRepository;
import com.mongsom.dev.repository.ProductImgRepository;
import com.mongsom.dev.repository.ProductRepository;
import com.mongsom.dev.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    
    private final CartRepository cartRepository;
    private final ProductImgRepository productImgRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    
 // CartService 새로운 버전 - combinationId 기반

    /**
     * 장바구니 추가
     */
    @Transactional
    public RespDto<String> addToCart(CartAddReqDto reqDto) {
        try {
            log.info("장바구니 추가 시작 - userCode: {}, productId: {}, combinationId: {}", 
                    reqDto.getUserCode(), reqDto.getProductId(), reqDto.getCombinationId());
            
            // 1. 사용자 존재 확인
            if (!userRepository.existsByUserCode(reqDto.getUserCode())) {
                log.warn("존재하지 않는 사용자 - userCode: {}", reqDto.getUserCode());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("존재하지 않는 사용자입니다.")
                        .build();
            }
            
            // 2. 상품 존재 확인
            Optional<Product> productOpt = productRepository.findByIdOnly(reqDto.getProductId());
            if (productOpt.isEmpty()) {
                log.warn("존재하지 않는 상품 - productId: {}", reqDto.getProductId());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("존재하지 않는 상품입니다.")
                        .build();
            }
            
            Product product = productOpt.get();
            
            // 3. 상품 상태 확인
            if (product.getDeleteStatus() == 1) {
                return RespDto.<String>builder()
                        .code(-1)
                        .data("삭제된 상품입니다.")
                        .build();
            }
            
            if (product.getIsAvailable() == 0) {
                return RespDto.<String>builder()
                        .code(-1)
                        .data("판매가 중단된 상품입니다.")
                        .build();
            }
            
            // 4. 옵션 조합 확인 (combinationId가 있는 경우)
            if (reqDto.getCombinationId() != null) {
                // 옵션 조합 존재 여부 및 재고 상태 확인
                if (!isValidOptionCombination(reqDto.getProductId(), reqDto.getCombinationId())) {
                    return RespDto.<String>builder()
                            .code(-1)
                            .data("유효하지 않은 옵션 조합입니다.")
                            .build();
                }
            }
            
            // 5. 기존 장바구니 아이템 확인
            Optional<Cart> existingCartOpt = findExistingCart(reqDto.getUserCode(), reqDto.getProductId(), reqDto.getCombinationId());
            
            if (existingCartOpt.isPresent()) {
                // 기존 아이템이 있으면 수량 증가
                Cart existingCart = existingCartOpt.get();
                existingCart.increaseQuantity(reqDto.getQuantity());
                cartRepository.save(existingCart);
                
                log.info("기존 장바구니 수량 증가 - cartId: {}, 새 수량: {}", 
                        existingCart.getCartId(), existingCart.getQuantity());
            } else {
                // 새 아이템 추가
                Cart newCart = Cart.createCart(
                        reqDto.getUserCode(),
                        reqDto.getProductId(), 
                        reqDto.getCombinationId(),
                        reqDto.getQuantity()
                );
                cartRepository.save(newCart);
                
                log.info("새 장바구니 아이템 추가 - cartId: {}", newCart.getCartId());
            }
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("장바구니에 추가되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("장바구니 추가 실패", e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("장바구니 추가 중 오류가 발생했습니다.")
                    .build();
        }
    }

    /**
     * 장바구니 조회
     */
    public RespDto<CartRespDto> getCart(Long userCode) {
        try {
            log.info("장바구니 조회 시작 - userCode: {}", userCode);
            
            // 1. 장바구니 아이템들 조회
            List<Cart> cartItems = cartRepository.findByUserCodeWithDetails(userCode);
            
            // 2. DTO 변환
            List<CartRespDto.CartItemDto> cartItemDtos = cartItems.stream()
                    .map(this::convertToCartItemDto)
                    .collect(Collectors.toList());
            
            // 3. 응답 생성
            CartRespDto responseData = CartRespDto.from(cartItemDtos);
            
            log.info("장바구니 조회 완료 - 아이템 수: {}, 총 금액: {}", 
                    cartItemDtos.size(), responseData.getCartSummary().getTotalPrice());
            
            return RespDto.<CartRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("장바구니 조회 실패 - userCode: {}", userCode, e);
            return RespDto.<CartRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }

    /**
     * 장바구니 수량 변경
     */
    @Transactional
    public RespDto<String> updateQuantity(CartUpdateQuantityReqDto reqDto) {
        try {
            log.info("장바구니 수량 변경 시작 - userCode: {}, productId: {}, quantity: {}", 
                    reqDto.getUserCode(), reqDto.getProductId(), reqDto.getQuantity());
            
            // 수량 업데이트
            int updatedRows;
            if (reqDto.getCombinationId() != null) {
                updatedRows = cartRepository.updateQuantityByUserCodeAndProductIdAndCombinationId(
                        reqDto.getUserCode(), reqDto.getProductId(), reqDto.getCombinationId(), reqDto.getQuantity());
            } else {
                updatedRows = cartRepository.updateQuantityByUserCodeAndProductIdAndCombinationIdIsNull(
                        reqDto.getUserCode(), reqDto.getProductId(), reqDto.getQuantity());
            }
            
            if (updatedRows == 0) {
                log.warn("장바구니 아이템을 찾을 수 없음 - userCode: {}, productId: {}, combinationId: {}", 
                        reqDto.getUserCode(), reqDto.getProductId(), reqDto.getCombinationId());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("장바구니 아이템을 찾을 수 없습니다.")
                        .build();
            }
            
            log.info("수량 변경 완료 - 업데이트된 행: {}", updatedRows);
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("수량이 변경되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("수량 변경 실패", e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("수량 변경 중 오류가 발생했습니다.")
                    .build();
        }
    }

    /**
     * 장바구니 아이템 삭제
     */
    @Transactional
    public RespDto<String> deleteCartItem(Long userCode, Integer productId, Integer combinationId) {
        try {
            log.info("장바구니 아이템 삭제 시작 - userCode: {}, productId: {}, combinationId: {}", 
                    userCode, productId, combinationId);
            
            if (combinationId != null) {
                cartRepository.deleteByUserCodeAndProductIdAndCombinationId(userCode, productId, combinationId);
            } else {
                cartRepository.deleteByUserCodeAndProductIdAndCombinationIdIsNull(userCode, productId);
            }
            
            log.info("장바구니 아이템 삭제 완료");
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("장바구니에서 삭제되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("장바구니 삭제 실패", e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("삭제 중 오류가 발생했습니다.")
                    .build();
        }
    }

    // === Private Helper Methods ===

    private Optional<Cart> findExistingCart(Long userCode, Integer productId, Integer combinationId) {
        if (combinationId != null) {
            return cartRepository.findByUserCodeAndProductIdAndCombinationId(userCode, productId, combinationId);
        } else {
            return cartRepository.findByUserCodeAndProductIdAndCombinationIdIsNull(userCode, productId);
        }
    }

    private boolean isValidOptionCombination(Integer productId, Integer combinationId) {
        // TODO: ProductOptionCombination 테이블에서 유효성 검증
        // 현재는 단순히 true 반환
        return true;
    }

    private CartRespDto.CartItemDto convertToCartItemDto(Cart cart) {
        // 상품 기본 정보
        Product product = cart.getProduct();
        String mainImageUrl = null;
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            mainImageUrl = product.getProductImages().get(0).getProductImgUrl();
        }
        
        // 옵션 정보 및 가격 계산
        List<CartRespDto.OptionInfo> selectedOptions = new ArrayList<>();
        Integer optionPrice = 0;
        
        if (cart.hasOptions() && cart.getOptionCombination() != null) {
            // TODO: 옵션 조합에서 옵션 정보 추출
            // 현재는 기본값 사용
            optionPrice = 0;
        }
        
        // 가격 계산
        Integer unitPrice = product.getDiscountPrice() + optionPrice;
        Integer totalPrice = unitPrice * cart.getQuantity();
        
        return CartRespDto.CartItemDto.builder()
                .cartId(cart.getCartId())
                .userCode(cart.getUserCode())
                .productId(cart.getProductId())
                .productName(product.getName())
                .basePrice(product.getBasePrice())
                .discountPrice(product.getDiscountPrice())
                .combinationId(cart.getCombinationId())
                .selectedOptions(selectedOptions)
                .optionPrice(optionPrice)
                .unitPrice(unitPrice)
                .quantity(cart.getQuantity())
                .totalPrice(totalPrice)
                .checkStatus(cart.getCheckStatus())
                .mainImageUrl(mainImageUrl)
                .createdAt(cart.getCreatedAt())
                .build();
    }
    
    
    
    
//    //장바구니체크변경
//    @Transactional
//    public RespDto<String> changeCheckStatus(CartChangeCheckReqDto reqDto) {
//        try {
//            // 1. 사용자 존재 확인
//            Optional<User> userOpt = userRepository.findUserByUserCode(reqDto.getUserCode());
//            if (userOpt.isEmpty()) {
//                return RespDto.<String>builder()
//                        .code(-1)
//                        .data("존재하지 않는 사용자입니다")
//                        .build();
//            }
//            
//            // 2. 장바구니 아이템 조회 (productId + optId 조합으로)
//            List<Cart> cartItems;
//            if (reqDto.getOptId() != null) {
//                cartItems = cartRepository.findByUserCodeAndProductIdAndOptId(
//                        reqDto.getUserCode(), reqDto.getProductId(), reqDto.getOptId());
//            } else {
//                cartItems = cartRepository.findByUserCodeAndProductIdAndOptIdIsNull(
//                        reqDto.getUserCode(), reqDto.getProductId());
//            }
//            
//            if (cartItems.isEmpty()) {
//                return RespDto.<String>builder()
//                        .code(-1)
//                        .data("해당 장바구니 아이템을 찾을 수 없습니다")
//                        .build();
//            }
//            
//            // 3. 체크 상태 토글 (현재 상태의 반대로 변경)
//            Cart cart = cartItems.get(0);
//            Integer currentStatus = cart.getCheckStatus();
//            Integer newStatus = 0 ;
//            if(currentStatus == 0) {
//            	newStatus = 1;
//            } else {
//            	newStatus = 0;
//            }
//            
//            cart.setCheckStatus(newStatus);
//            cartRepository.save(cart);
//            
//            String statusMessage = newStatus == 1 ? "체크됨" : "체크 해제됨";
//            
//            return RespDto.<String>builder()
//                    .code(1)
//                    .data("체크 상태가 변경되었습니다 (" + statusMessage + ")")
//                    .build();
//                    
//        } catch (Exception e) {
//            return RespDto.<String>builder()
//                    .code(-1)
//                    .data("체크 상태 변경 중 오류가 발생했습니다")
//                    .build();
//        }
//    }
//    
//    // 장바구니 전체 체크 상태 변경
//    @Transactional
//    public RespDto<String> updateAllCheckStatus(CartAllCheckReqDto reqDto) {
//        try {
//            log.info("=== 장바구니 전체 체크 상태 변경 시작 - userCode: {}, allCheckStatus: {} ===", 
//                    reqDto.getUserCode(), reqDto.getAllCheckStatus());
//            
//            // 1. allCheckStatus 값 검증
//            if (reqDto.getAllCheckStatus() != 0 && reqDto.getAllCheckStatus() != 1) {
//                log.warn("잘못된 체크 상태 값 - userCode: {}, allCheckStatus: {}", 
//                        reqDto.getUserCode(), reqDto.getAllCheckStatus());
//                return RespDto.<String>builder()
//                        .code(-1)
//                        .data("체크 상태는 0(해제) 또는 1(선택)이어야 합니다")
//                        .build();
//            }
//            
//            // 2. 사용자 존재 확인
//            Optional<User> userOpt = userRepository.findUserByUserCode(reqDto.getUserCode());
//            if (userOpt.isEmpty()) {
//                log.warn("존재하지 않는 사용자 - userCode: {}", reqDto.getUserCode());
//                return RespDto.<String>builder()
//                        .code(-1)
//                        .data("존재하지 않는 사용자입니다")
//                        .build();
//            }
//            
//            // 3. 해당 사용자의 장바구니 존재 확인
//            List<Cart> cartItems = cartRepository.findByUserCodeOrderByCreatedAtDesc(reqDto.getUserCode());
//            if (cartItems.isEmpty()) {
//                log.warn("장바구니가 비어있습니다 - userCode: {}", reqDto.getUserCode());
//                return RespDto.<String>builder()
//                        .code(-1)
//                        .data("장바구니가 비어있습니다")
//                        .build();
//            }
//            
//            // 4. 전체 체크 상태 업데이트 (Integer 값으로 직접 전달)
//            int updatedCount = cartRepository.updateAllCheckStatusByUserCode(
//                    reqDto.getUserCode(), reqDto.getAllCheckStatus());
//            
//            if (updatedCount > 0) {
//                String statusMessage = (reqDto.getAllCheckStatus() == 1) ? "전체 선택" : "전체 해제";
//                log.info("장바구니 전체 체크 상태 변경 완료 - userCode: {}, 변경된 아이템 수: {}, 상태: {}", 
//                        reqDto.getUserCode(), updatedCount, statusMessage);
//                
//                return RespDto.<String>builder()
//                        .code(1)
//                        .data("장바구니 " + statusMessage + "이 완료되었습니다")
//                        .build();
//            } else {
//                log.error("장바구니 전체 체크 상태 변경 실패 - userCode: {}", reqDto.getUserCode());
//                return RespDto.<String>builder()
//                        .code(-1)
//                        .data("장바구니 전체 체크 상태 변경에 실패했습니다")
//                        .build();
//            }
//            
//        } catch (Exception e) {
//            log.error("장바구니 전체 체크 상태 변경 실패 - userCode: {}, allCheckStatus: {}, error: {}", 
//                    reqDto.getUserCode(), reqDto.getAllCheckStatus(), e.getMessage());
//            return RespDto.<String>builder()
//                    .code(-1)
//                    .data("장바구니 전체 체크 상태 변경 중 오류가 발생했습니다")
//                    .build();
//        }
//    }
    
}