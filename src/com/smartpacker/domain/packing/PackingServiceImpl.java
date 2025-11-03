package com.smartpacker.domain.packing;

import com.smartpacker.domain.item.Item;
import com.smartpacker.exception.DatabaseException;
import com.smartpacker.repository.MyClosetRepository;
import com.smartpacker.repository.PackingListRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PackingService의 구현 클래스입니다.
 */
public class PackingServiceImpl implements PackingService {

    // 의존성 주입
    private final PackingListRepository packingListRepository;
    private final MyClosetRepository myClosetRepository;
    private final AnalysisEngine analysisEngine;
    
    
    /**
     * 생성자를 통해 필요한 Repository와 Engine 객체를 주입받습니다.
     * @param packingListRepository PackingListRepository 구현체
     * @param myClosetRepository MyClosetRepository 구현체
     * @param analysisEngine AnalysisEngine 객체
     */
    public PackingServiceImpl(PackingListRepository packingListRepository,
                              MyClosetRepository myClosetRepository,
                              AnalysisEngine analysisEngine) {
        this.packingListRepository = packingListRepository;
        this.myClosetRepository = myClosetRepository;
        this.analysisEngine = analysisEngine;
    }

    @Override
    public PackingList createPackingList(String userId, String listName, String tags, List<Item> items) throws DatabaseException {
        PackingList newPackingList = new PackingList(userId, listName, tags, items);
        return packingListRepository.save(newPackingList);
    }

    @Override
    public List<PackingList> getMyPackingLists(String userId) throws DatabaseException {
        return packingListRepository.findAllByUserId(userId);
    }

    @Override
    public void sharePackingList(long listId, boolean isShared) throws DatabaseException {
        packingListRepository.updateSharedStatus(listId, isShared);
    }

    @Override
    public List<String> getAiRecommendations(String userId, String[] travelProfileTags, List<Item> currentItems) throws DatabaseException {
        // 1. 추천 분석에 필요한 '내 옷장' 정보를 가져옵니다.
        Map<String, Item> myCloset = myClosetRepository.findAllByUserId(userId);

        // 2. AnalysisEngine에 필요한 모든 데이터를 전달하고 추천 로직 실행을 요청합니다.
        return analysisEngine.recommendItems(
                travelProfileTags,
                currentItems,
                myCloset
        );
    }
    
    @Override
    public PackingAnalysisResult analyzePackingList(PackingList packingList, String userId) throws DatabaseException {
        Map<String, Item> closetItems = myClosetRepository.findAllByUserId(userId);
        List<Item> packedItems = packingList.getItems();
        
        if (packedItems.isEmpty()) {
            return new PackingAnalysisResult(); // 분석할 아이템이 없음
        }
        
        int closetItemCount = 0;
        Set<String> missingFromCloset = new HashSet<>();
        
        for (Item packedItem : packedItems) {
            if (closetItems.containsKey(packedItem.getName())) {
                closetItemCount++;
            } else {
                missingFromCloset.add(packedItem.getName());
            }
        }
        
        PackingAnalysisResult result = new PackingAnalysisResult();
        result.closetUtilization = (double) closetItemCount / packedItems.size();
        result.missingFromCloset = missingFromCloset;
        
        return result;
    }
    
    @Override
    public List<String> generateFinalCheckReport(PackingList packingList, List<String> recommendations) {
        List<String> report = new ArrayList<>();
        List<String> packedItemNames = packingList.getItems().stream()
                .map(Item::getName)
                .collect(Collectors.toList());

        // 1. 빠뜨린 필수품 점검
        List<String> missingEssentials = recommendations.stream()
                .filter(rec -> rec.startsWith("[필수!]"))
                .map(this::parseItemNameFromRecommendation)
                .filter(itemName -> !packedItemNames.contains(itemName))
                .collect(Collectors.toList());

        if (missingEssentials.isEmpty()) {
            report.add("[OK] 필수품: 모든 필수 아이템을 챙기셨습니다.");
        } else {
            report.add("[주의!] 빠뜨린 필수품이 있습니다: " + String.join(", ", missingEssentials));
        }

        // 2. 꿀팁 아이템 채택 여부 점검
        long packedTipCount = recommendations.stream()
                .filter(rec -> rec.startsWith("[꿀팁]"))
                .map(this::parseItemNameFromRecommendation)
                .filter(packedItemNames::contains)
                .count();

        if (packedTipCount > 0) {
            report.add("[Good] 꿀팁 아이템: 다른 여행자들의 지혜를 " + packedTipCount + "개 챙기셨네요!");
        } else {
            report.add("[제안] 꿀팁 아이템: AI 추천 아이템을 추가하면 여행이 더 편해질 수 있습니다.");
        }
        
        // 3. 종합 피드백
        if (missingEssentials.isEmpty()) {
            report.add("  >> 총평: 훌륭한 리스트입니다! 즐거운 여행 되세요.");
        } else {
            report.add("  >> 총평: 저장하기 전에 빠뜨린 필수품이 없는지 다시 확인해보세요!");
        }

        return report;
    }

    // AI 추천 문자열에서 아이템 이름을 파싱하는 헬퍼 메소드
    private String parseItemNameFromRecommendation(String rec) {
        String temp = rec.substring(rec.indexOf("]") + 1).trim();
        if (temp.contains("(")) {
            return temp.substring(0, temp.indexOf("(")).trim();
        }
        return temp;
    }
    
    @Override
    public void updatePackingList(PackingList packingList) throws DatabaseException {
        // 나중에 유효성 검사 등 비즈니스 로직 추가 가능
        packingListRepository.update(packingList);
    }
    
    @Override
    public List<PackingList> getAllSharedLists() throws DatabaseException {
        // Service 계층은 단순 호출만 담당
        return packingListRepository.findAllShared();
    }
}