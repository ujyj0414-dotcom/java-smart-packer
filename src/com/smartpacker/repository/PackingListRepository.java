package com.smartpacker.repository;

import com.smartpacker.domain.packing.PackingList;
import com.smartpacker.exception.DatabaseException;
import java.util.List;

/**
 * 패킹 리스트 데이터에 접근하기 위한 인터페이스입니다.
 */
public interface PackingListRepository {

    /**
     * 새로운 패킹 리스트를 데이터베이스에 저장합니다.
     * @param packingList 저장할 패킹 리스트 객체
     * @return 저장 후 자동 생성된 ID를 포함한 패킹 리스트 객체
     * @throws DatabaseException DB 오류 발생 시
     */
    PackingList save(PackingList packingList) throws DatabaseException;
    
    /**
     * 패킹 리스트의 공유 상태를 업데이트합니다.
     * @param listId 공유 상태를 변경할 리스트의 ID
     * @param isShared 새로운 공유 상태 (true/false)
     * @throws DatabaseException DB 오류 발생 시
     */
    void updateSharedStatus(long listId, boolean isShared) throws DatabaseException;

    /**
     * 특정 사용자가 생성한 모든 패킹 리스트를 조회합니다.
     * @param userId 사용자 ID
     * @return 해당 사용자의 패킹 리스트 목록
     * @throws DatabaseException DB 오류 발생 시
     */
    List<PackingList> findAllByUserId(String userId) throws DatabaseException;

    /**
     * 추천 분석을 위해, 공유된(is_shared=true) 패킹 리스트 중
     * 주어진 태그와 유사한 리스트들을 조회합니다.
     * @param tags 분석용 태그 배열 (e.g., ["업무", "여름", "3일"])
     * @return 조건에 맞는 공유 패킹 리스트 목록
     * @throws DatabaseException DB 오류 발생 시
     */
    List<PackingList> findSharedListsByTags(String[] tags) throws DatabaseException;
    
    /**
     * is_shared가 true로 설정된 모든 패킹 리스트를 조회합니다.
     * (AI 추천 엔진의 학습 데이터셋 조회용)
     * @return 공유된 패킹 리스트의 List
     * @throws DatabaseException DB 조회 중 오류 발생 시
     */
    List<PackingList> findAllShared() throws DatabaseException;
    
    /**
     * 개발용: 추천 엔진 분석을 위한 가짜 공유 데이터를 대량으로 삽입합니다.
     * 이미 데이터가 있으면 중복 삽입하지 않습니다.
     * @throws DatabaseException DB 오류 발생 시
     */
    void seedSharedLists() throws DatabaseException;
    
    void deleteAllData() throws DatabaseException;
    
    void update(PackingList packingList) throws DatabaseException;
    
}