package com.mongsom.dev.service.admin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mongsom.dev.common.dto.RespDto;
import com.mongsom.dev.dto.admin.user.reqDto.AdminLoginReqDto;
import com.mongsom.dev.dto.admin.user.reqDto.MileageChargeReqDto;
import com.mongsom.dev.dto.admin.user.respDto.AdminUserListRespDto;
import com.mongsom.dev.dto.admin.user.respDto.MileageChargeRespDto;
import com.mongsom.dev.entity.User;
import com.mongsom.dev.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 관리자 회원 정보 조회 (페이지네이션 + 검색)
     * 
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기
     * @param searchItem 검색어 (이름 또는 전화번호 부분검색, null/empty면 전체 조회)
     * @return 회원 목록 및 페이지네이션 정보
     */
    public RespDto<AdminUserListRespDto> getUserList(Integer page, Integer size, String searchItem) {
        try {
            log.info("=== 관리자 회원 정보 조회 시작 - page: {}, size: {}, searchItem: '{}' ===", 
                    page, size, searchItem);
            
            // 페이지 번호는 1부터 시작하지만 Spring Data JPA는 0부터 시작
            Pageable pageable = PageRequest.of(page, size, Sort.by("userCode").descending());
            
            // 검색어 유무에 따른 조회 분기
            Page<User> userPage;
            
            if (searchItem != null && !searchItem.trim().isEmpty()) {
                // 검색어가 있는 경우: 이름 또는 전화번호로 검색
                String trimmedSearchItem = searchItem.trim();
                log.info("검색 조건 적용 - searchItem: '{}'", trimmedSearchItem);
                
                userPage = userRepository.findActiveUsersByNameOrPhone(trimmedSearchItem, pageable);
                
                log.info("검색 결과 - 총 {}건 중 {}페이지 조회, 조회된 사용자: {}명", 
                        userPage.getTotalElements(), page, userPage.getNumberOfElements());
                
            } else {
                // 검색어가 없는 경우: 전체 조회 (기존 로직)
                log.info("전체 사용자 조회");
                userPage = userRepository.findActiveUsers(pageable);
            }
            
            // User 엔티티를 UserInfo DTO로 변환
            List<AdminUserListRespDto.UserInfo> userInfoList = userPage.getContent().stream()
                    .map(user -> AdminUserListRespDto.UserInfo.builder()
                            .userCode(user.getUserCode())
                            .userId(user.getUserId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .mileage(user.getMileage())
                            .build())
                    .collect(Collectors.toList());
            
            // 페이지네이션 정보 생성
            AdminUserListRespDto.Pagination pagination = AdminUserListRespDto.Pagination.builder()
                    .currentPage(page)
                    .totalPage(userPage.getTotalPages())
                    .size(size)
                    .hasNext(userPage.hasNext())
                    .build();
            
            // 최종 응답 DTO 생성
            AdminUserListRespDto responseDto = AdminUserListRespDto.builder()
                    .users(userInfoList)
                    .pagination(pagination)
                    .build();
            
            String searchInfo = (searchItem != null && !searchItem.trim().isEmpty()) 
                    ? String.format("검색어: '%s', ", searchItem.trim())
                    : "";
            
            log.info("=== 회원 정보 조회 완료 - {}조회된 회원 수: {}, 전체 페이지: {}, 다음 페이지 존재: {} ===", 
                    searchInfo, userInfoList.size(), userPage.getTotalPages(), userPage.hasNext());
            
            return RespDto.<AdminUserListRespDto>builder()
                    .code(1)
                    .data(responseDto)
                    .build();
            
        } catch (Exception e) {
            log.error("회원 정보 조회 실패 - page: {}, size: {}, searchItem: '{}', error: {}", 
                    page, size, searchItem, e.getMessage(), e);
            
            return RespDto.<AdminUserListRespDto>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * 관리자 로그인
     * @param reqDto 아이디, 비밀번호
     * @return 로그인 성공 시 관리자 정보, 실패 시 null
     */
    public RespDto<Long> adminLogin(AdminLoginReqDto reqDto) {
        try {
            log.info("=== 관리자 로그인 시도 - userId: {} ===", reqDto.getUserId());
            
            // 1. userId와 provider='ADMIN'으로 사용자 조회
            Optional<User> adminOpt = userRepository.findAdminByUserId(reqDto.getUserId());
            
            if (adminOpt.isEmpty()) {
                log.warn("관리자를 찾을 수 없음 - userId: {}", reqDto.getUserId());
                return RespDto.<Long>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            User admin = adminOpt.get();
            
            // 2. 비밀번호 확인
            if (!passwordEncoder.matches(reqDto.getPassword(), admin.getPassword())) {
                log.warn("비밀번호 불일치 - userId: {}", reqDto.getUserId());
                return RespDto.<Long>builder()
                        .code(-1)
                        .data(null)
                        .build();
            }
            
            // 3. 로그인 성공 - 관리자 정보 반환
            log.info("=== 관리자 로그인 성공 - userCode: {}, userId: {} ===", 
                    admin.getUserCode(), admin.getUserId());
            
            return RespDto.<Long>builder()
                    .code(1)
                    .data(admin.getUserCode())
                    .build();
            
        } catch (Exception e) {
            log.error("관리자 로그인 실패 - userId: {}, error: {}", 
                    reqDto.getUserId(), e.getMessage(), e);
            
            return RespDto.<Long>builder()
                    .code(-1)
                    .data(null)
                    .build();
        }
    }
    
    /**
     * 사용자 마일리지 충전
     */
    @Transactional
    public RespDto<MileageChargeRespDto> chargeMileage(MileageChargeReqDto reqDto) {
        try {
            log.info("마일리지 충전 시작 - userCode: {}, chargeAmount: {}", 
                    reqDto.getUserCode(), reqDto.getChargeAmount());
            
            // 1. 사용자 존재 확인
            Optional<User> userOpt = userRepository.findUserByUserCode(reqDto.getUserCode());
            if (userOpt.isEmpty()) {
                log.warn("존재하지 않는 사용자 - userCode: {}", reqDto.getUserCode());
                return RespDto.<MileageChargeRespDto>builder()
                        .code(-1)
                        .data(MileageChargeRespDto.failure("존재하지 않는 사용자입니다."))
                        .build();
            }
            
            User user = userOpt.get();
            Integer beforeMileage = user.getMileage();
            
            // 2. 마일리지 충전
            user.addMileage(reqDto.getChargeAmount());
            userRepository.save(user);
            
            Integer afterMileage = user.getMileage();
            
            log.info("마일리지 충전 완료 - userCode: {}, 충전 전: {}, 충전 후: {}, 충전액: {}",
                    reqDto.getUserCode(), beforeMileage, afterMileage, reqDto.getChargeAmount());
            
            // 3. 응답 데이터 생성
            MileageChargeRespDto responseData = MileageChargeRespDto.success(
                user.getUserCode(),
                user.getName(),
                beforeMileage,
                afterMileage,
                reqDto.getChargeAmount()
            );
            
            return RespDto.<MileageChargeRespDto>builder()
                    .code(1)
                    .data(responseData)
                    .build();
            
        } catch (Exception e) {
            log.error("마일리지 충전 실패 - userCode: {}, chargeAmount: {}", 
                    reqDto.getUserCode(), reqDto.getChargeAmount(), e);
            
            return RespDto.<MileageChargeRespDto>builder()
                    .code(-1)
                    .data(MileageChargeRespDto.failure("마일리지 충전 중 오류가 발생했습니다: " + e.getMessage()))
                    .build();
        }
    }

}