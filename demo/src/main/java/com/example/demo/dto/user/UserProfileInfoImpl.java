package com.example.demo.dto.user;

import com.example.demo.entity.QuestionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * UserProfileInfo 구현체
 * MSA 전환 시 UserService에서 반환하는 데이터 구조
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileInfoImpl implements UserProfileInfo {
    
    private String userId;
    private QuestionCategory.MajorCategory learningPurpose;
    private List<QuestionCategory.MinorCategory> selectedMinorCategories;
    private LearningStyle learningStyle;
    private DifficultyLevel difficultyPreference;
    private boolean hasInterestsSet;
    
    @Override
    public String getUserId() { return userId; }
    
    @Override
    public QuestionCategory.MajorCategory getLearningPurpose() { return learningPurpose; }
    
    @Override
    public List<QuestionCategory.MinorCategory> getSelectedMinorCategories() { return selectedMinorCategories; }
    
    @Override
    public LearningStyle getLearningStyle() { return learningStyle; }
    
    @Override
    public DifficultyLevel getDifficultyPreference() { return difficultyPreference; }
    
    @Override
    public boolean hasInterestsSet() { return hasInterestsSet; }
    
    /**
     * 더미 프로필 생성 (테스트용)
     */
    public static UserProfileInfoImpl createDummyProfile(String userId) {
        return UserProfileInfoImpl.builder()
                .userId(userId)
                .learningPurpose(QuestionCategory.MajorCategory.BUSINESS)
                .selectedMinorCategories(List.of(
                    QuestionCategory.MinorCategory.MEETING_CONFERENCE,
                    QuestionCategory.MinorCategory.CUSTOMER_SERVICE
                ))
                .learningStyle(LearningStyle.VISUAL)
                .difficultyPreference(DifficultyLevel.INTERMEDIATE)
                .hasInterestsSet(true)
                .build();
    }
}
