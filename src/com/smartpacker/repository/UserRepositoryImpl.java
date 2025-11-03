package com.smartpacker.repository;

import com.smartpacker.domain.user.User;
import com.smartpacker.exception.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRepositoryImpl implements UserRepository {
	
	private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);
	
	 /**
     * 외부에서 Connection 객체를 주입받을 수 있도록 메소드를 분리합니다. (protected)
     * 이렇게 하면 테스트 시에 가짜 Connection을 주입할 수 있습니다.
     * @return Connection 객체
     * @throws SQLException
     */
    protected Connection getConnection() throws SQLException {
        return JdbcManager.getConnection();
    }


    @Override
    public void setupDatabase() throws DatabaseException {
        String createUserTableSql = "CREATE TABLE IF NOT EXISTS users (user_id TEXT PRIMARY KEY, password_hash TEXT NOT NULL, password_salt TEXT NOT NULL);";
        String createMyClosetTableSql = "CREATE TABLE IF NOT EXISTS my_closet (user_id TEXT NOT NULL, item_name TEXT NOT NULL, category TEXT NOT NULL, quantity INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (user_id, item_name), FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE);";
        String createPackingListsTableSql = "CREATE TABLE IF NOT EXISTS packing_lists (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id TEXT NOT NULL, list_name TEXT NOT NULL, tags TEXT, items_json TEXT NOT NULL, is_shared BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE);";

        try (Connection conn = this.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUserTableSql);
            stmt.execute(createMyClosetTableSql);
            stmt.execute(createPackingListsTableSql);
        } catch (SQLException e) {
            throw new DatabaseException("데이터베이스 테이블 초기화 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public void save(User user) throws DatabaseException {
        String sql = "INSERT INTO users (user_id, password_hash, password_salt) VALUES (?, ?, ?)";
        log.debug("Executing SQL: {}", sql); // DEBUG 레벨로 SQL 쿼리 로깅
        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getPasswordSalt());
            int affectedRows = pstmt.executeUpdate();
            log.debug("DB에 저장 완료. 영향 받은 행: {}", affectedRows);
        } catch (SQLException e) {
        	 log.error("User 저장 실패: {}", user.getUserId(), e);
             throw new DatabaseException(user.getUserId() + " 사용자 등록 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public Optional<User> findByUserId(String userId) throws DatabaseException {
        String sql = "SELECT user_id, password_hash, password_salt FROM users WHERE user_id = ?";
        log.debug("Executing SQL: {} with parameter: {}", sql, userId);
        
        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    log.debug("User 찾음: {}", userId);
                    
                    // 주석을 아래의 실제 코드로 교체합니다.
                    User user = new User(
                            rs.getString("user_id"),
                            rs.getString("password_hash"),
                            rs.getString("password_salt")
                    );
                    return Optional.of(user);
                } else {
                    log.debug("User 찾지 못함: {}", userId);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            log.error("User 조회 실패: {}", userId, e);
            throw new DatabaseException(userId + " 사용자 조회 중 오류가 발생했습니다.", e);
        }
    }
}
