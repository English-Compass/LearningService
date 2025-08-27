package com.example.demo.client;

import com.example.demo.dto.user.UserProfileInfo;
import com.example.demo.dto.user.UserProfileInfoImpl;
import com.example.demo.entity.QuestionCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserServiceClient 구현체
 * 현재는 더미 데이터 반환, MSA 전환 시 실제 API 호출로 변경
 */
@Service
@Slf4j
public class UserServiceClientImpl implements UserServiceClient {
    
    @Override
    public UserProfileInfo getUserProfile(String userId) {
        log.info("더미 사용자 프로필 조회: userId={}", userId);
        
        // 더미 사용자 프로필 생성
        return UserProfileInfoImpl.builder()
            .userId(userId)
            .learningPurpose(QuestionCategory.MajorCategory.BUSINESS)
            .selectedMinorCategories(List.of(
                QuestionCategory.MinorCategory.MEETING_CONFERENCE,
                QuestionCategory.MinorCategory.CUSTOMER_SERVICE
            ))
            .learningStyle(UserProfileInfo.LearningStyle.AUDITORY)
            .difficultyPreference(UserProfileInfo.DifficultyLevel.INTERMEDIATE)
            .hasInterestsSet(true)
            .build();
    }
    
    @Override
    public UserProfileInfo getUserInterests(String userId) {
        log.info("더미 사용자 관심사 조회: userId={}", userId);
        
        // getUserProfile과 동일한 더미 데이터 반환
        return getUserProfile(userId);
    }
}
