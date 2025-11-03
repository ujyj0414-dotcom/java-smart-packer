package com.smartpacker.domain.packing;

import com.smartpacker.domain.item.Item;
import com.smartpacker.domain.item.ItemFactory;
import com.smartpacker.exception.DatabaseException;
import com.smartpacker.repository.PackingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisEngineTest {

    private AnalysisEngine engine;
    private Map<String, Item> myClosetItems;
    private List<Item> currentUserItems;
    private List<PackingList> similarPackingListsData; // 데이터 저장용

    // --- 가짜(Fake) Repository 구현 ---
    // 실제 DB에 연결하지 않고, 미리 정해둔 데이터만 반환하는 가짜 객체
    private static class FakePackingListRepository implements PackingListRepository {
        private final List<PackingList> db;

        public FakePackingListRepository(List<PackingList> fakeDatabase) {
            this.db = fakeDatabase;
        }

        @Override
        public List<PackingList> findSharedListsByTags(String[] tags) throws DatabaseException {
            // 태그와 무관하게 항상 준비된 데이터를 반환하도록 단순화
            return db;
        }
        
        // 나머지 메소드들은 이 테스트에서 사용하지 않으므로 비워둡니다.
        @Override public PackingList save(PackingList packingList) { return null; }
        @Override public void updateSharedStatus(long listId, boolean isShared) {}
        @Override public List<PackingList> findAllByUserId(String userId) { return null; }
        @Override public void seedSharedLists() {}

		@Override
		public void deleteAllData() throws DatabaseException {
		}

		@Override
		public void update(PackingList packingList) throws DatabaseException {
		}

		@Override
		public List<PackingList> findAllShared() throws DatabaseException {
			return null;
		}
    }


    @BeforeEach
    void setUp() {
        Arrays.asList("여권", "지갑");

        myClosetItems = new HashMap<>();
        myClosetItems.put("선크림", ItemFactory.create("선크림", "화장품", 2));
        myClosetItems.put("보조 배터리", ItemFactory.create("보조 배터리", "전자기기", 1));

        currentUserItems = Arrays.asList(
                ItemFactory.create("지갑", "필수품", 1),
                ItemFactory.create("보조 배터리", "전자기기", 1)
        );
        
        // 유사 여행자 데이터 (정식 PackingList 객체 사용)
        similarPackingListsData = Arrays.asList(
            new PackingList("user1", "list1", "", Arrays.asList(ItemFactory.create("여권", "필수품", 1), ItemFactory.create("보조 배터리", "전자기기", 1), ItemFactory.create("선크림", "화장품", 1))),
            new PackingList("user2", "list2", "", Arrays.asList(ItemFactory.create("여권", "필수품", 1), ItemFactory.create("보조 배터리", "전자기기", 1), ItemFactory.create("선크림", "화장품", 1))),
            new PackingList("user3", "list3", "", Arrays.asList(ItemFactory.create("여권", "필수품", 1), ItemFactory.create("보조 배터리", "전자기기", 1), ItemFactory.create("선크림", "화장품", 1), ItemFactory.create("멀티탭", "전자기기", 1))),
            new PackingList("user4", "list4", "", Arrays.asList(ItemFactory.create("여권", "필수품", 1), ItemFactory.create("보조 배터리", "전자기기", 1), ItemFactory.create("선크림", "화장품", 1), ItemFactory.create("멀티탭", "전자기기", 1))),
            new PackingList("user5", "list5", "", Arrays.asList(ItemFactory.create("여권", "필수품", 1), ItemFactory.create("보조 배터리", "전자기기", 1), ItemFactory.create("선크림", "화장품", 1), ItemFactory.create("멀티탭", "전자기기", 1))),
            new PackingList("user6", "list6", "", Arrays.asList(ItemFactory.create("여권", "필수품", 1), ItemFactory.create("보조 배터리", "전자기기", 1), ItemFactory.create("선크림", "화장품", 1))),
            new PackingList("user7", "list7", "", Arrays.asList(ItemFactory.create("여권", "필수품", 1), ItemFactory.create("보조 배터리", "전자기기", 1), ItemFactory.create("선크림", "화장품", 1))),
            new PackingList("user8", "list8", "", Arrays.asList(ItemFactory.create("여권", "필수품", 1), ItemFactory.create("보조 배터리", "전자기기", 1), ItemFactory.create("멀티탭", "전자기기", 1))),
            new PackingList("user9", "list9", "", Arrays.asList(ItemFactory.create("여권", "필수품", 1), ItemFactory.create("보조 배터리", "전자기기", 1))),
            new PackingList("user10", "list10", "", Arrays.asList(ItemFactory.create("여권", "필수품", 1), ItemFactory.create("멀티탭", "전자기기", 1)))
        );

        // --- 의존성 주입 ---
        // 1. 가짜 Repository를 생성하고, 가짜 데이터를 넣어줍니다.
        PackingListRepository fakeRepository = new FakePackingListRepository(similarPackingListsData);
        // 2. AnalysisEngine을 생성할 때, 진짜 Repository 대신 가짜 Repository를 주입합니다.
        engine = new AnalysisEngine(fakeRepository);
    }

    @Test
    @DisplayName("빠뜨린 필수품을 최우선으로 추천해야 한다")
    void recommendItems_shouldRecommendMissingEssentialFirst() throws DatabaseException { // throws 추가
        // given
        String[] travelProfileTags = {"여름", "휴가"};

        // when
        List<String> recommendations = engine.recommendItems(travelProfileTags, currentUserItems, myClosetItems);

        // then
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.get(0).contains("[필수!] 여권"));
    }
    
    // 나머지 테스트 메소드들도 throws DatabaseException 을 추가해야 할 수 있습니다.
    // ... (이하 다른 @Test 메소드들은 기존과 거의 동일하나, recommendItems 호출 시 travelProfileTags를 넘겨주는 부분만 다름)

    @Test
    @DisplayName("꿀팁 추천 시, '내 옷장' 보유 여부를 표시해야 한다")
    void recommendItems_shouldShowClosetInfoForTips() throws DatabaseException {
        String[] travelProfileTags = {"여름", "휴가"};
        List<String> recommendations = engine.recommendItems(travelProfileTags, currentUserItems, myClosetItems);

        String suncreamRec = recommendations.stream().filter(s -> s.contains("선크림")).findFirst().orElse(null);
        assertNotNull(suncreamRec);
        assertTrue(suncreamRec.contains("(회원님은 2개 보유 중)"));
    }

    @Test
    @DisplayName("이미 챙긴 아이템은 꿀팁 추천에서 제외해야 한다")
    void recommendItems_shouldExcludeAlreadyPackedItemsFromTips() throws DatabaseException {
        String[] travelProfileTags = {"여름", "휴가"};
        List<String> recommendations = engine.recommendItems(travelProfileTags, currentUserItems, myClosetItems);

        boolean isRecommendingPackedItem = recommendations.stream().anyMatch(rec -> rec.startsWith("[꿀팁]") && rec.contains("보조 배터리"));
        assertFalse(isRecommendingPackedItem);
    }

    @Test
    @DisplayName("꿀팁 추천은 빈도수 순서로 정렬되어야 한다")
    void recommendItems_shouldBeSortedByFrequency() throws DatabaseException {
        String[] travelProfileTags = {"여름", "휴가"};
        List<String> recommendations = engine.recommendItems(travelProfileTags, currentUserItems, myClosetItems);
        
        List<String> tipRecommendations = recommendations.stream().filter(s -> s.startsWith("[꿀팁]")).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        int suncreamIndex = -1, multiTapIndex = -1;
        for (int i = 0; i < tipRecommendations.size(); i++) {
            if (tipRecommendations.get(i).contains("선크림")) suncreamIndex = i;
            if (tipRecommendations.get(i).contains("멀티탭")) multiTapIndex = i;
        }

        assertTrue(suncreamIndex != -1 && multiTapIndex != -1);
        assertTrue(suncreamIndex < multiTapIndex);
    }
}