package com.smartpacker.domain.user;

import com.smartpacker.domain.item.Item;
import com.smartpacker.exception.DatabaseException;
import com.smartpacker.util.FileImportResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MyClosetService {
    Map<String, Item> getMyClosetItems(String userId) throws DatabaseException;
    void addItemToMyCloset(String userId, Item item) throws DatabaseException;
    boolean removeItemFromMyCloset(String userId, String itemName) throws DatabaseException;
    
    /**
     * '내 옷장'의 카테고리별 아이템 개수 통계를 반환합니다.
     * @param userId 사용자 ID
     * @return 카테고리 이름을 Key로, 아이템 개수를 Value로 갖는 Map
     * @throws DatabaseException DB 오류 발생 시
     */
    Map<String, Long> getMyClosetStatistics(String userId) throws DatabaseException;
    
    FileImportResult addItemsToMyClosetFromFile(String userId, String filePath) throws DatabaseException, IOException;
    
    List<Item> findSimilarItemsInMyCloset(String userId, String keyword);
}

