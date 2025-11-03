package com.smartpacker.repository;

import com.smartpacker.domain.item.Item;
import com.smartpacker.exception.DatabaseException;
import java.util.List;
import java.util.Map;

/**
 * '내 옷장' 데이터에 접근하기 위한 인터페이스입니다.
 */
public interface MyClosetRepository {

    /**
     * 특정 사용자의 '내 옷장'에 아이템을 저장하거나, 이미 존재하면 수량을 업데이트합니다.
     * @param userId 사용자 ID
     * @param item 저장할 아이템 객체
     * @throws DatabaseException DB 오류 발생 시
     */
    void save(String userId, Item item) throws DatabaseException;

    /**
     * 특정 사용자의 '내 옷장'에 있는 모든 아이템을 조회합니다.
     * @param userId 사용자 ID
     * @return 아이템 이름(String)을 Key로, Item 객체를 Value로 갖는 Map
     * @throws DatabaseException DB 오류 발생 시
     */
    Map<String, Item> findAllByUserId(String userId) throws DatabaseException;

    /**
     * 특정 사용자의 '내 옷장'에서 아이템을 삭제합니다.
     * @param userId 사용자 ID
     * @param itemName 삭제할 아이템 이름
     * @return 삭제에 성공하면 true, 해당 아이템이 없으면 false
     * @throws DatabaseException DB 오류 발생 시
     */
    boolean delete(String userId, String itemName) throws DatabaseException;
    
    /**
     * 특정 사용자의 '내 옷장' 아이템 통계를 카테고리별로 조회합니다.
     * @param userId 사용자 ID
     * @return 카테고리 이름을 Key로, 아이템 개수(Long)를 Value로 갖는 Map
     * @throws DatabaseException DB 오류 발생 시
     */
    Map<String, Long> getCategoryStatistics(String userId) throws DatabaseException;

    /**
     * 텍스트 파일로부터 여러 아이템을 '내 옷장'에 일괄 등록합니다.
     * @param userId 사용자 ID
     * @param items 등록할 아이템 리스트
     * @return 성공적으로 등록된 아이템 개수
     * @throws DatabaseException DB 오류 발생 시
     */
    int batchInsert(String userId, List<Item> items) throws DatabaseException;
    
    void deleteAllData() throws DatabaseException;
    
    List<Item> findItemsByNameLike(String userId, String keyword);
}