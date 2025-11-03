package com.smartpacker.domain.packing;

import com.smartpacker.domain.item.Item;
import com.smartpacker.exception.DatabaseException;
import java.util.List;
import java.util.Set;

/**
 * 패킹 리스트 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface PackingService {

    /**
     * 새로운 패킹 리스트를 생성하고 저장합니다.
     * @param userId 사용자 ID
     * @param listName 리스트 이름
     * @param tags 여행 프로필 태그
     * @param items 아이템 목록
     * @return 저장된 PackingList 객체
     * @throws DatabaseException DB 오류 발생 시
     */
    PackingList createPackingList(String userId, String listName, String tags, List<Item> items) throws DatabaseException;

    /**
     * 특정 사용자의 모든 패킹 리스트를 조회합니다.
     * @param userId 사용자 ID
     * @return 해당 사용자의 패킹 리스트 목록
     * @throws DatabaseException DB 오류 발생 시
     */
    List<PackingList> getMyPackingLists(String userId) throws DatabaseException;

    /**
     * 특정 패킹 리스트의 공유 상태를 변경합니다.
     * @param listId 리스트 ID
     * @param isShared 공유 여부
     * @throws DatabaseException DB 오류 발생 시
     */
    void sharePackingList(long listId, boolean isShared) throws DatabaseException;
    
    void updatePackingList(PackingList packingList) throws DatabaseException;

    /**
     * AI 추천 엔진을 호출하여 추천 아이템 목록을 반환합니다.
     * @param userId 현재 사용자 ID (내 옷장 조회를 위해 필요)
     * @param travelProfileTags 여행 프로필 태그
     * @param currentItems 사용자의 현재 리스트에 담긴 아이템들
     * @return 추천 문구가 포함된 문자열 리스트
     * @throws DatabaseException DB 오류 발생 시
     */
    List<String> getAiRecommendations(String userId, String[] travelProfileTags, List<Item> currentItems) throws DatabaseException;
    
 // 분석 리포트용 데이터 구조 (DTO 역할)
    class PackingAnalysisResult {
        public double closetUtilization;
        public Set<String> missingFromCloset;
        // 필요 시 다른 분석 결과 추가
    }
    
    /**
     * AI 학습 데이터로 사용되는 모든 공유 리스트를 조회합니다.
     * @return 공유된 패킹 리스트의 List
     * @throws DatabaseException
     */
    List<PackingList> getAllSharedLists() throws DatabaseException;

    PackingAnalysisResult analyzePackingList(PackingList packingList, String userId) throws DatabaseException;
    
    List<String> generateFinalCheckReport(PackingList packingList, List<String> recommendations);
}