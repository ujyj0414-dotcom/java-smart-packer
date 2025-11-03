package com.smartpacker.domain.packing;

import com.smartpacker.domain.item.Item;
import com.smartpacker.exception.DatabaseException;
import com.smartpacker.repository.PackingListRepository; 
import com.smartpacker.config.AppConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 유사 여행자 데이터를 분석하여 패킹 아이템을 추천하는 엔진 클래스입니다.
 * 2단계부터는 PackingListRepository를 통해 실제 DB 데이터로 분석합니다.
 */
public class AnalysisEngine {

    // 의존성: PackingListRepository에 의존
    private final PackingListRepository packingListRepository;

    /**
     * 생성자를 통해 외부에서 PackingListRepository 구현체를 주입받습니다.
     * @param packingListRepository 사용할 PackingListRepository 객체
     */
    public AnalysisEngine(PackingListRepository packingListRepository) {
        this.packingListRepository = packingListRepository;
    }

    /**
     * 분석을 기반으로 사용자에게 추천 아이템 목록을 제안합니다.
     *
     * @param travelProfileTags    사용자가 생성한 여행 프로필 태그 배열 (e.g., ["업무", "여름", "3일"])
     * @param currentUserItems     사용자의 현재 패킹 리스트에 있는 아이템 목록
     * @param myClosetItems        사용자의 '내 옷장'에 있는 아이템 목록 (소유 여부 확인용)
     * @param essentialItems       개발자가 정의한 필수품 목록
     * @return 추천 아이템 목록 (문자열 리스트 형태)
     * @throws DatabaseException  DB 조회 중 오류 발생 시
     */
   
    		public List<String> recommendItems(String[] travelProfileTags, List<Item> currentUserItems, Map<String, Item> myClosetItems) throws DatabaseException { // throws 추가

        // --- DB에서 유사 여행자 데이터 조회 ---
        List<PackingList> similarPackingLists = packingListRepository.findSharedListsByTags(travelProfileTags);

        // 데이터가 부족할 경우 분석을 수행하지 않고 빈 리스트를 반환합니다.
        if (similarPackingLists == null || similarPackingLists.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> recommendations = new ArrayList<>();

        // --- 1. 필수품 우선 제안 ---
        List<String> currentUserItemNames = currentUserItems.stream()
                .map(Item::getName)
                .collect(Collectors.toList());

        for (String essential : AppConfig.ESSENTIAL_ITEMS) {
            if (!currentUserItemNames.contains(essential)) {
                recommendations.add("[필수!] " + essential + " (잊으셨나요?)");
            }
        }

        // --- 2. '꿀팁' 제안 (데이터 기반 빈도수 분석) ---

        // 2-1. 유사 여행자들의 아이템 빈도수 집계 (HashMap 사용)
        Map<String, Integer> itemFrequency = new HashMap<>();
        for (PackingList list : similarPackingLists) {
            for (Item item : list.getItems()) {
                itemFrequency.put(item.getName(), itemFrequency.getOrDefault(item.getName(), 0) + 1);
            }
        }

        // 2-2. 빈도수 기준으로 아이템 정렬
        List<Map.Entry<String, Integer>> sortedItems = new ArrayList<>(itemFrequency.entrySet());
        sortedItems.sort(Map.Entry.comparingByValue(Comparator.reverseOrder())); // 빈도수 높은 순으로 정렬

        // 2-3. 최종 제안 목록 생성 (필터링)
        int recommendationCount = 0;

        for (Map.Entry<String, Integer> entry : sortedItems) {
            if (recommendationCount >= AppConfig.MAX_AI_RECOMMENDATIONS) {
                break;
            }

            String itemName = entry.getKey();
            int frequency = entry.getValue();

            // 필터링 조건:
            // (1) 이미 사용자의 리스트에 있는 아이템은 제외
            // (2) 이미 필수품으로 분류된 아이템도 제외
            if (!currentUserItemNames.contains(itemName) && !AppConfig.ESSENTIAL_ITEMS.contains(itemName)) {
                // '내 옷장' DB와 교차 검증
                String closetInfo = "";
                if (myClosetItems.containsKey(itemName)) {
                    int quantityInCloset = myClosetItems.get(itemName).getQuantity();
                    closetInfo = String.format(" (회원님은 %d개 보유 중)", quantityInCloset);
                }

                // (e.g., "[꿀팁!] 보조 배터리 (87%의 여행자가 챙겼어요) (회원님은 1개 보유 중)")
                double percentage = (double) frequency / similarPackingLists.size() * 100;
                recommendations.add(String.format("[꿀팁] %s (%.0f%%의 여행자가 챙겼어요)%s",
                        itemName, percentage, closetInfo));

                recommendationCount++;
            }
        }

        return recommendations;
    }

    
}