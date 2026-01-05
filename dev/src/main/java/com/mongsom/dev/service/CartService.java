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
import com.mongsom.dev.dto.cart.reqDto.CartAllCheckReqDto;
import com.mongsom.dev.dto.cart.reqDto.CartUpdateQuantityReqDto;
import com.mongsom.dev.dto.cart.respDto.CartRespDto;
import com.mongsom.dev.entity.Cart;
import com.mongsom.dev.entity.Product;
import com.mongsom.dev.entity.ProductImg;
import com.mongsom.dev.entity.ProductOptionType;
import com.mongsom.dev.entity.ProductOptionValue;
import com.mongsom.dev.entity.User;
import com.mongsom.dev.repository.CartRepository;
import com.mongsom.dev.repository.ProductImgRepository;
import com.mongsom.dev.repository.ProductOptionTypeRepository;
import com.mongsom.dev.repository.ProductOptionValueRepository;
import com.mongsom.dev.repository.ProductRepository;
import com.mongsom.dev.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
    private final ProductOptionValueRepository productOptionValueRepository;
    private final ProductOptionTypeRepository productOptionTypeRepository;
    
    /**
     * 장바구니 추가 (가격 계산 포함)
     */
    @Transactional
    public RespDto<String> addToCart(CartAddReqDto reqDto) {
        try {
            log.info("장바구니 추가 시작 - userCode: {}, productId: {}, option1: {}, option2: {}", 
                    reqDto.getUserCode(), reqDto.getProductId(), reqDto.getOption1(), reqDto.getOption2());
            
            // 1. 사용자 존재 확인
            if (!userRepository.existsByUserCode(reqDto.getUserCode())) {
                log.warn("존재하지 않는 사용자 - userCode: {}", reqDto.getUserCode());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("존재하지 않는 사용자입니다.")
                        .build();
            }
            
            // 2. 상품 조회 및 상태 확인
            Optional<Product> productOpt = productRepository.findByIdOnly(reqDto.getProductId());
            if (productOpt.isEmpty()) {
                log.warn("존재하지 않는 상품 - productId: {}", reqDto.getProductId());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("존재하지 않는 상품입니다.")
                        .build();
            }
            
            Product product = productOpt.get();
            
            // 상품 상태 확인
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
            
            // 3. 가격 계산
            PriceCalculation priceCalc = calculateCartPrice(product, reqDto.getOption1(), reqDto.getOption2());
            if (priceCalc == null) {
                return RespDto.<String>builder()
                        .code(-1)
                        .data("옵션 정보를 확인할 수 없습니다.")
                        .build();
            }
            
            // 4. 기존 장바구니 아이템 확인
            Optional<Cart> existingCartOpt = cartRepository.findByUserCodeAndProductIdAndOption1AndOption2(
                    reqDto.getUserCode(), reqDto.getProductId(), reqDto.getOption1(), reqDto.getOption2());
            
            if (existingCartOpt.isPresent()) {
                // 기존 아이템이 있으면 수량 증가 (가격은 업데이트)
                Cart existingCart = existingCartOpt.get();
                existingCart.setQuantity(existingCart.getQuantity() + reqDto.getQuantity());
                
                // 가격 정보 업데이트 (상품 가격이 변경되었을 수도 있으므로)
                existingCart.setBasePrice(priceCalc.getBasePrice());
                existingCart.setOptionPrice(priceCalc.getOptionPrice());
                existingCart.setTotalUnitPrice(priceCalc.getTotalUnitPrice());
                
                cartRepository.save(existingCart);
                
                log.info("기존 장바구니 수량 증가 - cartId: {}, 새 수량: {}, 개당가격: {}", 
                        existingCart.getCartId(), existingCart.getQuantity(), priceCalc.getTotalUnitPrice());
            } else {
                // 새 아이템 추가
                Cart newCart = Cart.builder()
                        .userCode(reqDto.getUserCode())
                        .productId(reqDto.getProductId())
                        .option1(reqDto.getOption1())
                        .option2(reqDto.getOption2())
                        .quantity(reqDto.getQuantity())
                        .basePrice(priceCalc.getBasePrice())
                        .optionPrice(priceCalc.getOptionPrice())
                        .totalUnitPrice(priceCalc.getTotalUnitPrice())
                        .checkStatus(1)
                        .build();
                
                cartRepository.save(newCart);
                
                log.info("새 장바구니 아이템 추가 - cartId: {}, 개당가격: {}", 
                        newCart.getCartId(), priceCalc.getTotalUnitPrice());
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
     * 가격 계산 (상품 + 옵션)
     */
    private PriceCalculation calculateCartPrice(Product product, Integer option1Id, Integer option2Id) {
        try {
            // 1. 상품 기본 가격 (할인된 가격)
            Integer basePrice = product.getDiscountPrice();
            
            // 2. 옵션 가격 계산
            Integer optionPrice = 0;
            
            // option1 가격 조회
            if (option1Id != null) {
                Optional<ProductOptionValue> option1Opt = productOptionValueRepository.findById(option1Id);
                if (option1Opt.isEmpty()) {
                    log.warn("존재하지 않는 option1 - optionValueId: {}", option1Id);
                    return null;
                }
                
                ProductOptionValue option1 = option1Opt.get();
                
                // 해당 상품의 옵션인지 확인
                if (!isValidOptionForProduct(product.getProductId(), option1Id)) {
                    log.warn("상품에 속하지 않는 option1 - productId: {}, optionValueId: {}", 
                            product.getProductId(), option1Id);
                    return null;
                }
                
                optionPrice += option1.getPriceAdjustment();
            }
            
            // option2 가격 조회
            if (option2Id != null) {
                Optional<ProductOptionValue> option2Opt = productOptionValueRepository.findById(option2Id);
                if (option2Opt.isEmpty()) {
                    log.warn("존재하지 않는 option2 - optionValueId: {}", option2Id);
                    return null;
                }
                
                ProductOptionValue option2 = option2Opt.get();
                
                // 해당 상품의 옵션인지 확인
                if (!isValidOptionForProduct(product.getProductId(), option2Id)) {
                    log.warn("상품에 속하지 않는 option2 - productId: {}, optionValueId: {}", 
                            product.getProductId(), option2Id);
                    return null;
                }
                
                optionPrice += option2.getPriceAdjustment();
            }
            
            // 3. 총 개당 가격 계산
            Integer totalUnitPrice = basePrice + optionPrice;
            
            log.info("가격 계산 완료 - basePrice: {}, optionPrice: {}, totalUnitPrice: {}", 
                    basePrice, optionPrice, totalUnitPrice);
            
            return PriceCalculation.builder()
                    .basePrice(basePrice)
                    .optionPrice(optionPrice)
                    .totalUnitPrice(totalUnitPrice)
                    .build();
            
        } catch (Exception e) {
            log.error("가격 계산 실패", e);
            return null;
        }
    }

    /**
     * 옵션이 해당 상품에 속하는지 확인
     */
    private boolean isValidOptionForProduct(Integer productId, Integer optionValueId) {
        return productOptionValueRepository.existsByOptionValueIdAndProductId(optionValueId, productId);
    }

    /**
     * 가격 계산 결과 내부 클래스
     */
    @Data
    @Builder
    @AllArgsConstructor
    private static class PriceCalculation {
        private Integer basePrice;      // 상품 기본 가격 (할인된 가격)
        private Integer optionPrice;    // 옵션 추가 가격
        private Integer totalUnitPrice; // 개당 총 가격
    }

    /**
     * Cart를 CartItemDto로 변환 (수정된 버전)
     */
    private CartRespDto.CartItemDto convertToCartItemDto(Cart cart) {
        // 상품 기본 정보
        Product product = cart.getProduct();
        String mainImageUrl = null;
        if (product != null && product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            mainImageUrl = product.getProductImages().get(0).getProductImgUrl();
        }
        
        // 옵션 정보 수집 (저장된 가격 사용)
        List<CartRespDto.OptionInfo> selectedOptions = new ArrayList<>();
        
        if (cart.getOption1() != null) {
            Optional<ProductOptionValue> option1Opt = productOptionValueRepository.findById(cart.getOption1());
            option1Opt.ifPresent(optionValue -> {
                selectedOptions.add(CartRespDto.OptionInfo.builder()
                        .optionTypeName(getOptionTypeName(optionValue.getOptionTypeId()))
                        .optionValueName(optionValue.getValueName())
                        .priceAdjustment(optionValue.getPriceAdjustment())
                        .build());
            });
        }
        
        if (cart.getOption2() != null) {
            Optional<ProductOptionValue> option2Opt = productOptionValueRepository.findById(cart.getOption2());
            option2Opt.ifPresent(optionValue -> {
                selectedOptions.add(CartRespDto.OptionInfo.builder()
                        .optionTypeName(getOptionTypeName(optionValue.getOptionTypeId()))
                        .optionValueName(optionValue.getValueName())
                        .priceAdjustment(optionValue.getPriceAdjustment())
                        .build());
            });
        }
        
        // 저장된 가격 정보 사용 (DB에 저장된 계산된 값)
        Integer totalPrice = cart.getTotalUnitPrice() * cart.getQuantity();
        
        return CartRespDto.CartItemDto.builder()
                .cartId(cart.getCartId())
                .userCode(cart.getUserCode())
                .productId(cart.getProductId())
                .productName(product != null ? product.getName() : "알 수 없는 상품")
                .basePrice(cart.getBasePrice())          // DB 저장된 값
                .discountPrice(cart.getBasePrice())      // 할인된 가격이므로 동일
                .option1(cart.getOption1())
                .option2(cart.getOption2())
                .selectedOptions(selectedOptions)
                .optionPrice(cart.getOptionPrice())      // DB 저장된 값
                .unitPrice(cart.getTotalUnitPrice())     // DB 저장된 값
                .quantity(cart.getQuantity())
                .totalPrice(totalPrice)                  // 계산된 라인 총 가격
                .checkStatus(cart.getCheckStatus())
                .mainImageUrl(mainImageUrl)
                .createdAt(cart.getCreatedAt())
                .build();
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
            log.info("장바구니 수량 변경 시작 - cartId: {}, quantity: {}", 
                    reqDto.getCartId(), reqDto.getQuantity());
            
            // Cart 조회
            Optional<Cart> cartOpt = cartRepository.findById(reqDto.getCartId());
            if (cartOpt.isEmpty()) {
                log.warn("존재하지 않는 장바구니 아이템 - cartId: {}", reqDto.getCartId());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("장바구니 아이템을 찾을 수 없습니다.")
                        .build();
            }
            
            Cart cart = cartOpt.get();
            
            // 수량 업데이트
            cart.updateQuantity(reqDto.getQuantity());
            cartRepository.save(cart);
            
            log.info("수량 변경 완료 - cartId: {}, 새 수량: {}", reqDto.getCartId(), reqDto.getQuantity());
            
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
    public RespDto<String> deleteCartItem(Integer cartId) {
        try {
            log.info("장바구니 아이템 삭제 시작 - cartId: {}", cartId);
            
            // Cart 조회
            Optional<Cart> cartOpt = cartRepository.findById(cartId);
            if (cartOpt.isEmpty()) {
                log.warn("존재하지 않는 장바구니 아이템 - cartId: {}", cartId);
                return RespDto.<String>builder()
                        .code(-1)
                        .data("장바구니 아이템을 찾을 수 없습니다.")
                        .build();
            }
            
            Cart cart = cartOpt.get();
            
            cartRepository.delete(cart);
            
            log.info("장바구니 아이템 삭제 완료 - cartId: {}", cartId);
            
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

    private String getOptionTypeName(Integer optionTypeId) {
        return productOptionTypeRepository.findById(optionTypeId)
                .map(ProductOptionType::getTypeName)
                .orElse("알 수 없음");
    }
    
    
    
    
    /**
     * 개별 장바구니 체크 상태 토글 (0↔1)
     */
    @Transactional
    public RespDto<String> changeCheckStatus(Integer cartId) {
        try {
            log.info("장바구니 체크 상태 토글 시작 - cartId: {}", cartId);
            
            // Cart 조회
            Optional<Cart> cartOpt = cartRepository.findById(cartId);
            if (cartOpt.isEmpty()) {
                log.warn("존재하지 않는 장바구니 아이템 - cartId: {}", cartId);
                return RespDto.<String>builder()
                        .code(-1)
                        .data("장바구니 아이템을 찾을 수 없습니다.")
                        .build();
            }
            
            Cart cart = cartOpt.get();
            
            // 체크 상태 토글
            Integer oldStatus = cart.getCheckStatus();
            cart.toggleCheckStatus(); // 0->1, 1->0
            Integer newStatus = cart.getCheckStatus();
            
            cartRepository.save(cart);
            
            String statusText = newStatus == 1 ? "체크됨" : "체크해제";
            log.info("체크 상태 토글 완료 - cartId: {}, {} -> {} ({})", 
                    cartId, oldStatus, newStatus, statusText);
            
            return RespDto.<String>builder()
                    .code(1)
                    .data("체크 상태가 변경되었습니다.")
                    .build();
            
        } catch (Exception e) {
            log.error("체크 상태 변경 실패", e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("체크 상태 변경 중 오류가 발생했습니다.")
                    .build();
        }
    }

    /**
     * 전체 장바구니 체크 상태 일괄 변경
     */
    @Transactional
    public RespDto<String> changeAllCheckStatus(CartAllCheckReqDto reqDto) {
        try {
            log.info("전체 장바구니 체크 상태 변경 시작 - userCode: {}, allCheckStatus: {}", 
                    reqDto.getUserCode(), reqDto.getAllCheckStatus());
            
            // 사용자 존재 확인
            if (!userRepository.existsByUserCode(reqDto.getUserCode())) {
                log.warn("존재하지 않는 사용자 - userCode: {}", reqDto.getUserCode());
                return RespDto.<String>builder()
                        .code(-1)
                        .data("존재하지 않는 사용자입니다.")
                        .build();
            }
            
            // 전체 체크 상태 변경
            int updatedCount = cartRepository.updateAllCheckStatusByUserCode(
                    reqDto.getUserCode(), 
                    reqDto.getAllCheckStatus()
            );
            
            String statusText = reqDto.getAllCheckStatus() == 1 ? "전체선택" : "전체해제";
            log.info("전체 체크 상태 변경 완료 - userCode: {}, 변경된 아이템: {}개, 상태: {}", 
                    reqDto.getUserCode(), updatedCount, statusText);
            
            return RespDto.<String>builder()
                    .code(1)
                    .data(String.format("%s 처리되었습니다. (변경된 상품: %d개)", statusText, updatedCount))
                    .build();
            
        } catch (Exception e) {
            log.error("전체 체크 상태 변경 실패", e);
            return RespDto.<String>builder()
                    .code(-1)
                    .data("체크 상태 변경 중 오류가 발생했습니다.")
                    .build();
        }
    }
    
}