package com.mongsom.dev.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.cart.reqDto.CartAddReqDto;
import com.mongsom.dev.dto.cart.reqDto.CartUpdateQuantityReqDto;
import com.mongsom.dev.dto.cart.respDto.CartRespDto;
import com.mongsom.dev.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    
    private final CartService cartService;
    
 // CartController 새로운 버전 - combinationId 기반

    /**
     * 장바구니 추가
     */
    @PostMapping("/add")
    public ResponseEntity<RespDto<String>> addToCart(
            @Valid @RequestBody CartAddReqDto reqDto) {
        
        log.info("=== 장바구니 추가 요청 ===");
        log.info("userCode: {}, productId: {}, combinationId: {}, quantity: {}", 
                reqDto.getUserCode(), reqDto.getProductId(), reqDto.getCombinationId(), reqDto.getQuantity());
        
        RespDto<String> response = cartService.addToCart(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("장바구니 추가 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }

    /**
     * 장바구니 조회
     */
    @GetMapping("/{userCode}")
    public ResponseEntity<RespDto<CartRespDto>> getCart(
            @PathVariable("userCode") Long userCode) {
        
        log.info("=== 장바구니 조회 요청 ===");
        log.info("userCode: {}", userCode);
        
        RespDto<CartRespDto> response = cartService.getCart(userCode);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("장바구니 조회 결과 - code: {}", response.getCode());
        if (response.getData() != null && response.getData().getCartItems() != null) {
            log.info("장바구니 아이템 수: {}, 총 금액: {}", 
                    response.getData().getCartItems().size(),
                    response.getData().getCartSummary().getTotalPrice());
        }
        
        return ResponseEntity.status(status).body(response);
    }

    /**
     * 장바구니 수량 변경
     */
    @PutMapping("/update/quantity")
    public ResponseEntity<RespDto<String>> updateQuantity(
            @Valid @RequestBody CartUpdateQuantityReqDto reqDto) {
        
        log.info("=== 장바구니 수량 변경 요청 ===");
        log.info("userCode: {}, productId: {}, combinationId: {}, quantity: {}", 
                reqDto.getUserCode(), reqDto.getProductId(), reqDto.getCombinationId(), reqDto.getQuantity());
        
        RespDto<String> response = cartService.updateQuantity(reqDto);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("수량 변경 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }

    /**
     * 장바구니 아이템 삭제
     */
    @DeleteMapping("/delete/{userCode}/{productId}")
    public ResponseEntity<RespDto<String>> deleteCartItem(
            @PathVariable("userCode") Long userCode,
            @PathVariable("productId") Integer productId,
            @RequestParam(value = "combinationId", required = false) Integer combinationId) {
        
        log.info("=== 장바구니 아이템 삭제 요청 ===");
        log.info("userCode: {}, productId: {}, combinationId: {}", userCode, productId, combinationId);
        
        RespDto<String> response = cartService.deleteCartItem(userCode, productId, combinationId);
        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        log.info("아이템 삭제 결과 - code: {}", response.getCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
//    //장바구니체크변경
//    @PutMapping("/change/check")
//    public ResponseEntity<RespDto<String>> changeCheckStatus(
//            @Valid @RequestBody CartChangeCheckReqDto reqDto) {
//        RespDto<String> response = cartService.changeCheckStatus(reqDto);
//        return ResponseEntity.ok(response);
//    }
//    // 장바구니 전체 체크 상태 변경
//    @PutMapping("/change/check/all")
//    public ResponseEntity<RespDto<String>> updateAllCheckStatus(@Valid @RequestBody CartAllCheckReqDto reqDto) {
//        
//        String statusMessage = (reqDto.getAllCheckStatus() == 1) ? "전체 선택" : "전체 해제";
//        log.info("장바구니 전체 체크 상태 변경 요청 - userCode: {}, 상태: {}", 
//                reqDto.getUserCode(), statusMessage);
//        
//        RespDto<String> response = cartService.updateAllCheckStatus(reqDto);
//        HttpStatus status = response.getCode() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
//        return ResponseEntity.status(status).body(response);
//    }
}