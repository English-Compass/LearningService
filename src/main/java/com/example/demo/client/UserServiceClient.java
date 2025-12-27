package com.example.demo.client;

import com.example.demo.dto.user.UserProfileInfo;

/**
 * UserService API 호출을 위한 클라이언트 인터페이스
 * MSA 전환 시 UserService와의 통신에 사용
 * 현재는 더미 데이터 반환
 */
public interface UserServiceClient {
    
    /**
     * 사용자 프로필 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 프로필 정보
     */
    UserProfileInfo getUserProfile(String userId);
    
    /**
     * 사용자 관심사 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 관심사 정보
     */
    UserProfileInfo getUserInterests(String userId);
}
