package com.smartpacker.repository;

import com.smartpacker.domain.item.Item;
import com.smartpacker.domain.item.ItemFactory;
import com.smartpacker.exception.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyClosetRepositoryImpl implements MyClosetRepository {
    
    // Item 객체를 DB에 저장하기 위해 임시로 만든 헬퍼 클래스.
    // Item은 추상 클래스이므로 직접 new 할 수 없기 때문입니다.
    private static class ConcreteItem extends Item {
        public ConcreteItem(String name, String category, int quantity) {
            super(name, category, quantity);
        }
    }

    @Override
    public void save(String userId, Item item) throws DatabaseException {
        // SQLite의 INSERT OR REPLACE 구문을 사용하면, PK가 중복될 경우 UPDATE처럼 동작합니다.
        String sql = "INSERT OR REPLACE INTO my_closet (user_id, item_name, category, quantity) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = JdbcManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getCategory());
            pstmt.setInt(4, item.getQuantity());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("내 옷장 아이템 저장 중 오류가 발생했습니다.", e);
        } finally {
            JdbcManager.close(conn, pstmt);
        }
    }

    @Override
    public Map<String, Item> findAllByUserId(String userId) throws DatabaseException {
        String sql = "SELECT item_name, category, quantity FROM my_closet WHERE user_id = ?";
        Map<String, Item> closet = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = JdbcManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Item item = new ConcreteItem(
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getInt("quantity")
                );
                closet.put(item.getName(), item);
            }
        } catch (SQLException e) {
            throw new DatabaseException("내 옷장 조회 중 오류가 발생했습니다.", e);
        } finally {
            JdbcManager.close(conn, pstmt, rs);
        }
        return closet;
    }

    @Override
    public boolean delete(String userId, String itemName) throws DatabaseException {
        String sql = "DELETE FROM my_closet WHERE user_id = ? AND item_name = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = JdbcManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, itemName);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // 1개 이상의 행이 삭제되었다면 true 반환
        } catch (SQLException e) {
            throw new DatabaseException("내 옷장 아이템 삭제 중 오류가 발생했습니다.", e);
        } finally {
            JdbcManager.close(conn, pstmt);
        }
    }

    @Override
    public Map<String, Long> getCategoryStatistics(String userId) throws DatabaseException {
        // GROUP BY와 COUNT()를 사용한 통계 쿼리
        String sql = "SELECT category, COUNT(*) as count FROM my_closet WHERE user_id = ? GROUP BY category ORDER BY count DESC";
        Map<String, Long> stats = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = JdbcManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                stats.put(rs.getString("category"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("내 옷장 통계 조회 중 오류가 발생했습니다.", e);
        } finally {
            JdbcManager.close(conn, pstmt, rs);
        }
        return stats;
    }
    
    @Override
    public int batchInsert(String userId, List<Item> items) throws DatabaseException {
        String sql = "INSERT OR IGNORE INTO my_closet (user_id, item_name, category, quantity) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int successfulInserts = 0;

        try {
            conn = JdbcManager.getConnection();
            // 자동 커밋을 비활성화하여 트랜잭션 성능을 향상시킵니다.
            conn.setAutoCommit(false); 
            pstmt = conn.prepareStatement(sql);

            for (Item item : items) {
                pstmt.setString(1, userId);
                pstmt.setString(2, item.getName());
                pstmt.setString(3, item.getCategory());
                pstmt.setInt(4, item.getQuantity());
                pstmt.addBatch(); // 쿼리를 배치에 추가
            }

            int[] results = pstmt.executeBatch(); // 배치 실행
            conn.commit(); // 트랜잭션 커밋

            for (int result : results) {
                if (result >= 0) { // 성공한 경우 1 또는 0
                    successfulInserts++;
                }
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // 오류 발생 시 롤백
            } catch (SQLException ex) { /* 무시 */ }
            throw new DatabaseException("내 옷장 일괄 등록 중 오류가 발생했습니다.", e);
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true); // 자동 커밋 원상 복구
            } catch (SQLException e) { /* 무시 */ }
            JdbcManager.close(conn, pstmt);
        }
        return successfulInserts;
    }
    
    @Override
    public void deleteAllData() throws DatabaseException {
        String sql = "DELETE FROM my_closet";
        try (Connection conn = JdbcManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new DatabaseException("내 옷장 데이터 전체 삭제 중 오류 발생", e);
        }
    }
    
    @Override
    public List<Item> findItemsByNameLike(String userId, String keyword) {
        String sql = "SELECT item_name, category, quantity FROM my_closet WHERE user_id = ? AND item_name LIKE ?";
        List<Item> foundItems = new ArrayList<>();
        try (Connection conn = JdbcManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, "%" + keyword + "%"); // 키워드가 포함된 모든 아이템 검색
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                foundItems.add(ItemFactory.create(
                    rs.getString("item_name"),
                    rs.getString("category"),
                    rs.getInt("quantity")
                ));
            }
        } catch (SQLException e) {
            // 이 메소드는 치명적인 오류가 아니므로, 예외를 던지는 대신 빈 리스트를 반환하고 에러 로그만 남길 수 있습니다.
            System.err.println("[ERROR] 유사 아이템 검색 중 오류: " + e.getMessage());
        }
        return foundItems;
    }
}