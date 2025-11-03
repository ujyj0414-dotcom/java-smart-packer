package com.smartpacker.domain.user;

import com.smartpacker.domain.item.Item;
import com.smartpacker.exception.DatabaseException;
import com.smartpacker.repository.MyClosetRepository;
import com.smartpacker.util.FileIO;
import com.smartpacker.util.FileImportResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MyClosetServiceImpl implements MyClosetService {
    private final MyClosetRepository myClosetRepository;

    public MyClosetServiceImpl(MyClosetRepository myClosetRepository) {
        this.myClosetRepository = myClosetRepository;
    }

    @Override
    public Map<String, Item> getMyClosetItems(String userId) throws DatabaseException {
        return myClosetRepository.findAllByUserId(userId);
    }

    @Override
    public void addItemToMyCloset(String userId, Item item) throws DatabaseException {
        myClosetRepository.save(userId, item);
    }

    @Override
    public boolean removeItemFromMyCloset(String userId, String itemName) throws DatabaseException {
        return myClosetRepository.delete(userId, itemName);
    }
    
    @Override
    public Map<String, Long> getMyClosetStatistics(String userId) throws DatabaseException {
        return myClosetRepository.getCategoryStatistics(userId);
    }

    @Override
    public FileImportResult addItemsToMyClosetFromFile(String userId, String filePath)
            throws DatabaseException, IOException {

        // 1. FileIO 유틸리티를 사용하여 텍스트 파일 내용을 파싱합니다.
        // 이 결과 객체 안에는 성공한 아이템 리스트와 실패한 라인 정보가 모두 들어있습니다.
        FileImportResult result = FileIO.importFromTxt(filePath);

        // 2. 파싱에 성공한 아이템들이 하나라도 있다면,
        //    Repository의 batchInsert 메소드를 호출하여 DB에 한번에 저장합니다.
        if (!result.successItems.isEmpty()) {
            myClosetRepository.batchInsert(userId, result.successItems);
        }

        // 3. 성공/실패 정보가 모두 담긴 결과 객체를 Controller로 반환합니다.
        return result;
    }
    
    @Override
    public List<Item> findSimilarItemsInMyCloset(String userId, String keyword) {
        // 여기서는 Repository 호출만 하지만, 나중에 더 복잡한 비즈니스 로직(e.g., 검색 결과 순위 매기기) 추가 가능
        return myClosetRepository.findItemsByNameLike(userId, keyword);
    }

}