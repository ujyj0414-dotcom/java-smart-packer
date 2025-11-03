package com.smartpacker.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.smartpacker.domain.item.ClothingItem;
import com.smartpacker.domain.item.CosmeticItem;
import com.smartpacker.domain.item.ElectronicItem;
import com.smartpacker.domain.item.EntertainmentItem;
import com.smartpacker.domain.item.EssentialItem;
import com.smartpacker.domain.item.EtcItem;
import com.smartpacker.domain.item.FoodItem;
import com.smartpacker.domain.item.GeneralGoodsItem;
import com.smartpacker.domain.item.Item;
import com.smartpacker.domain.item.ItemFactory;
import com.smartpacker.domain.item.WorkItem;
import com.smartpacker.domain.packing.PackingList;
import com.smartpacker.exception.DatabaseException;
import com.smartpacker.util.RuntimeTypeAdapterFactory;

import java.lang.reflect.Type;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PackingListRepositoryImpl implements PackingListRepository {

	private final Gson gson; 
    private final Type itemListType = new TypeToken<ArrayList<Item>>() {}.getType();
    
    public PackingListRepositoryImpl() {
        // RuntimeTypeAdapterFactory를 아래와 같이 수정합니다.
        RuntimeTypeAdapterFactory<Item> adapter = RuntimeTypeAdapterFactory
            .of(Item.class, "category", true);
            // 이제 모든 타입은 고유(unique)합니다.
        adapter
            .registerSubtype(EssentialItem.class, "필수품")
            .registerSubtype(CosmeticItem.class, "화장품")
            .registerSubtype(GeneralGoodsItem.class, "잡화")
            .registerSubtype(EntertainmentItem.class, "엔터테인먼트")
            .registerSubtype(WorkItem.class, "업무")
            .registerSubtype(FoodItem.class, "식품")
            .registerSubtype(ClothingItem.class, "의류")
            .registerSubtype(ElectronicItem.class, "전자기기")
            .registerSubtype(EtcItem.class, "기타");

        this.gson = new GsonBuilder().registerTypeAdapterFactory(adapter).create();
    }
    
    @Override
    public PackingList save(PackingList packingList) throws DatabaseException {
        String sql = "INSERT INTO packing_lists (user_id, list_name, tags, items_json) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = JdbcManager.getConnection();
            // Statement.RETURN_GENERATED_KEYS 옵션으로 자동 생성된 ID를 받아올 수 있습니다.
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, packingList.getUserId());
            pstmt.setString(2, packingList.getListName());
            pstmt.setString(3, packingList.getTags());
            // Gson을 사용하여 List<Item> 객체를 JSON 문자열로 변환하여 저장
            pstmt.setString(4, gson.toJson(packingList.getItems(), itemListType));
            
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("패킹 리스트 생성 실패: 변경된 행이 없습니다.");
            }

            // 자동 생성된 ID(PK)를 ResultSet에서 가져와서 객체에 설정 후 반환
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    // 새로 생성된 PackingList 객체를 반환하기 위해 새 객체 생성 (불변성 유지)
                    return new PackingList(id, packingList.getUserId(), packingList.getListName(), packingList.getTags(),
                            packingList.getItems(), packingList.isShared(), LocalDateTime.now());
                } else {
                    throw new SQLException("패킹 리스트 생성 실패: ID를 가져올 수 없습니다.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("패킹 리스트 저장 중 오류가 발생했습니다.", e);
        } finally {
            JdbcManager.close(conn, pstmt);
        }
    }

    @Override
    public void updateSharedStatus(long listId, boolean isShared) throws DatabaseException {
        String sql = "UPDATE packing_lists SET is_shared = ? WHERE id = ?";
        try (Connection conn = JdbcManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isShared);
            pstmt.setLong(2, listId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("공유 상태 업데이트 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public List<PackingList> findAllByUserId(String userId) throws DatabaseException {
        String sql = "SELECT id, list_name, tags, items_json, is_shared, created_at FROM packing_lists WHERE user_id = ? ORDER BY created_at DESC";
        List<PackingList> lists = new ArrayList<>();
        
        try (Connection conn = JdbcManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lists.add(mapResultSetToPackingList(rs, userId));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("사용자의 패킹 리스트 조회 중 오류가 발생했습니다.", e);
        }
        return lists;
    }
    
    @Override
    public List<PackingList> findAllShared() throws DatabaseException {
        // 1. SQL 쿼리에서 user_id를 *추가*로 SELECT하고,
        //    WHERE 조건을 user_id = ? 대신 is_shared = true로 변경합니다.
        String sql = "SELECT id, user_id, list_name, tags, items_json, is_shared, created_at " +
                     "FROM packing_lists " +
                     "WHERE is_shared = true " +
                     "ORDER BY created_at DESC";
        
        List<PackingList> lists = new ArrayList<>();
        
        // 2. 님의 기존 DB 연결 방식(JdbcManager)을 그대로 사용합니다.
        try (Connection conn = JdbcManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 3. ResultSet에서 user_id를 가져옵니다.
                    String userId = rs.getString("user_id");
                    
                    // 4. 님이 이미 완벽하게 구현해 둔 mapResultSetToPackingList 헬퍼를
                    //    (Gson 역직렬화 포함) 그대로 재사용합니다.
                    lists.add(mapResultSetToPackingList(rs, userId));
                }
            }
        } catch (SQLException e) {
            // 님의 기존 예외 처리 방식을 그대로 따릅니다.
            throw new DatabaseException("모든 공유 리스트 조회 중 오류가 발생했습니다.", e);
        }
        return lists;
    }

    @Override
    public List<PackingList> findSharedListsByTags(String[] tags) throws DatabaseException {
        // WHERE ... LIKE ? OR ... LIKE ? 형태의 동적 쿼리 생성
        StringBuilder sqlBuilder = new StringBuilder("SELECT user_id, items_json FROM packing_lists WHERE is_shared = TRUE AND (");
        if (tags == null || tags.length == 0) {
            // 태그가 없는 경우 예외처리 또는 빈 리스트 반환
            return new ArrayList<>(); 
        }

        for (int i = 0; i < tags.length; i++) {
            sqlBuilder.append("tags LIKE ?");
            if (i < tags.length - 1) {
                sqlBuilder.append(" OR ");
            }
        }
        sqlBuilder.append(") LIMIT 100"); // 성능을 위해 최대 100개만 가져오도록 제한

        List<PackingList> lists = new ArrayList<>();
        try (Connection conn = JdbcManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            for (int i = 0; i < tags.length; i++) {
                pstmt.setString(i + 1, "%" + tags[i] + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 분석 엔진에서는 userId와 items 정보만 필요하므로, 일부 필드만 채워서 반환
                    String userId = rs.getString("user_id");
                    String itemsJson = rs.getString("items_json");
                    List<Item> items = gson.fromJson(itemsJson, itemListType);
                    // PackingList 생성자 중 items만 받는 것이 없으므로, 임시 데이터를 넣어줍니다.
                    lists.add(new PackingList(0, userId, "", "", items, true, null));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("공유 리스트 검색 중 오류가 발생했습니다.", e);
        }
        return lists;
    }
    
    // ResultSet의 현재 행을 PackingList 객체로 변환하는 헬퍼 메소드
    private PackingList mapResultSetToPackingList(ResultSet rs, String userId) throws SQLException {
        long id = rs.getLong("id");
        String listName = rs.getString("list_name");
        String tags = rs.getString("tags");
        String itemsJson = rs.getString("items_json");
        boolean isShared = rs.getBoolean("is_shared");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        
        // Gson을 사용하여 JSON 문자열을 List<Item> 객체로 변환
        List<Item> items = gson.fromJson(itemsJson, itemListType);

        return new PackingList(id, userId, listName, tags, items, isShared, createdAt);
    }
    
    @Override
    public void seedSharedLists() throws DatabaseException {
        String countSql = "SELECT COUNT(*) FROM packing_lists WHERE is_shared = TRUE";
        String insertSql = "INSERT INTO packing_lists (user_id, list_name, tags, items_json, is_shared) VALUES (?, ?, ?, ?, TRUE)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = JdbcManager.getConnection();

            // 1. 이미 Seeding 데이터가 있는지 확인합니다.
            pstmt = conn.prepareStatement(countSql);
            rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // 이미 데이터가 1개 이상 있다면, 중복 생성을 방지하기 위해 메소드를 종료합니다.
                System.out.println("[INFO] 이미 공유 데이터가 존재하여 Seeding을 건너뜁니다.");
                return;
            }
            
            System.out.println("[INFO] 공유 패킹 리스트 Seeding을 시작합니다...");

            // 2. 데이터가 없으면, 대량 삽입을 위해 트랜잭션을 시작합니다.
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(insertSql);

            // --- 가짜 데이터 생성 로직 ---
            List<PackingList> fakeLists = createFakePackingLists();
            for (PackingList list : fakeLists) {
                pstmt.setString(1, list.getUserId());
                pstmt.setString(2, list.getListName());
                pstmt.setString(3, list.getTags());
                pstmt.setString(4, gson.toJson(list.getItems(), itemListType));
                pstmt.addBatch();
            }

            // 3. 배치 실행 및 커밋
            pstmt.executeBatch();
            conn.commit();
            
            System.out.println("[SUCCESS] " + fakeLists.size() + "개의 공유 패킹 리스트 Seeding을 완료했습니다.");

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // 오류 발생 시 롤백
            } catch (SQLException ex) { /* 무시 */ }
            throw new DatabaseException("가짜 데이터 Seeding 중 오류가 발생했습니다.", e);
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true); // 자동 커밋 원상 복구
            } catch (SQLException e) { /* 무시 */ }
            // close(conn, pstmt, rs) 대신 각각 닫아줍니다. rs와 pstmt가 다른 쿼리에 사용되었기 때문입니다.
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* 무시 */ }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* 무시 */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* 무시 */ }
        }
    }

    /**
     * 다양한 시나리오의 가짜 PackingList 객체 50개를 생성하는 헬퍼 메소드입니다.
     */
    private List<PackingList> createFakePackingLists() {
        List<PackingList> lists = new ArrayList<>();

        // 시나리오 1: 여름 휴양지 (3박 4일) - 20개
        for (int i = 1; i <= 20; i++) {
            List<Item> items = new ArrayList<>(Arrays.asList(
                    ItemFactory.create("여권", "필수품", 1), ItemFactory.create("지갑", "필수품", 1),
                    ItemFactory.create("항공권", "필수품", 1), ItemFactory.create("반팔 티셔츠", "의류", 4),
                    ItemFactory.create("반바지", "의류", 2), ItemFactory.create("수영복", "의류", 1),
                    ItemFactory.create("선크림", "화장품", 1), ItemFactory.create("선글라스", "잡화", 1),
                    ItemFactory.create("스마트폰 충전기", "전자기기", 1)
            ));
            if (i % 2 == 0) items.add(ItemFactory.create("보조 배터리", "전자기기", 1));
            if (i % 3 == 0) items.add(ItemFactory.create("책", "엔터테인먼트", 1));
            lists.add(new PackingList("user" + i, "여름 휴가", "휴양,여름,4일", items));
        }
        
        // 시나리오 2: 겨울 도시 출장 (2박 3일) - 15개
        for (int i = 21; i <= 35; i++) {
            List<Item> items = new ArrayList<>(Arrays.asList(
                    ItemFactory.create("여권", "필수품", 1), ItemFactory.create("지갑", "필수품", 1),
                    ItemFactory.create("노트북", "전자기기", 1), ItemFactory.create("노트북 충전기", "전자기기", 1),
                    ItemFactory.create("스마트폰 충전기", "전자기기", 1), ItemFactory.create("보조 배터리", "전자기기", 1),
                    ItemFactory.create("히트텍", "의류", 2), ItemFactory.create("스웨터", "의류", 2),
                    ItemFactory.create("두꺼운 외투", "의류", 1), ItemFactory.create("명함", "업무", 20)
            ));
            if (i % 2 == 0) items.add(ItemFactory.create("멀티탭", "전자기기", 1));
            lists.add(new PackingList("user" + i, "서울 출장", "업무,겨울,3일", items));
        }
        
        // 시나리오 3: 가을 산악 트레킹 (1박 2일) - 15개
        for (int i = 36; i <= 50; i++) {
            List<Item> items = new ArrayList<>(Arrays.asList(
                    ItemFactory.create("지갑", "필수품", 1), ItemFactory.create("등산화", "의류", 1),
                    ItemFactory.create("등산복", "의류", 1), ItemFactory.create("방수 자켓", "의류", 1),
                    ItemFactory.create("에너지바", "식품", 4), ItemFactory.create("물통", "잡화", 1),
                    ItemFactory.create("보조 배터리", "전자기기", 1), ItemFactory.create("헤드랜턴", "잡화", 1)
            ));
            if (i % 3 == 0) items.add(ItemFactory.create("카메라", "전자기기", 1));
            lists.add(new PackingList("user" + i, "지리산 등반", "레저,가을,2일", items));
        }
        
        return lists;
    }
    
    @Override
    public void deleteAllData() throws DatabaseException {
        String sql = "DELETE FROM packing_lists";
        try (Connection conn = JdbcManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new DatabaseException("패킹 리스트 데이터 전체 삭제 중 오류 발생", e);
        }
    }
    
    @Override
    public void update(PackingList packingList) throws DatabaseException {
        // items_json과 tags 필드만 업데이트하도록 구현 (이름 등은 변경 불가)
        String sql = "UPDATE packing_lists SET items_json = ?, tags = ? WHERE id = ?";
        try (Connection conn = JdbcManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, gson.toJson(packingList.getItems(), itemListType));
            pstmt.setString(2, packingList.getTags());
            pstmt.setLong(3, packingList.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("패킹 리스트 업데이트 중 오류 발생", e);
        }
    }

}