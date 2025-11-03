package com.smartpacker.repository;

import com.smartpacker.domain.user.PasswordHasher;
import com.smartpacker.domain.user.User;
import com.smartpacker.exception.DatabaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private Connection connection;
    private UserRepository userRepository;

    /**
     * 테스트 전용 UserRepositoryImpl.
     * try-with-resources를 사용하지 않고 Connection을 수동으로 관리하여,
     * 테스트 메소드 내에서 Connection이 닫히는 것을 방지한다.
     */
    private static class TestUserRepositoryImpl extends UserRepositoryImpl {
        private final Connection testConnection;

        public TestUserRepositoryImpl(Connection connection) {
            this.testConnection = connection;
        }

        @Override
        protected Connection getConnection() {
            return this.testConnection;
        }

        // save 메소드를 오버라이드하여 Connection을 닫지 않도록 함
        @Override
        public void save(User user) throws DatabaseException {
            String sql = "INSERT INTO users (user_id, password_hash, password_salt) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
                pstmt.setString(1, user.getUserId());
                pstmt.setString(2, user.getPasswordHash());
                pstmt.setString(3, user.getPasswordSalt());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new DatabaseException("Test save failed", e);
            }
        }
        
        // findByUserId도 오버라이드
        @Override
        public Optional<User> findByUserId(String userId) throws DatabaseException {
            String sql = "SELECT user_id, password_hash, password_salt FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
                pstmt.setString(1, userId);
                try(ResultSet rs = pstmt.executeQuery()) {
                     if (rs.next()) {
                        return Optional.of(new User(rs.getString(1), rs.getString(2), rs.getString(3)));
                    }
                }
            } catch (SQLException e) {
                 throw new DatabaseException("Test find failed", e);
            }
            return Optional.empty();
        }
    }


    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE users (user_id TEXT PRIMARY KEY, password_hash TEXT NOT NULL, password_salt TEXT NOT NULL);");
        }
        // 테스트용 구현체를 생성하여 주입
        userRepository = new TestUserRepositoryImpl(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    @DisplayName("사용자 저장 및 ID로 조회 테스트")
    void saveAndFindByUserId() throws DatabaseException {
        User newUser = new User("testuser", "hash", "salt");

        userRepository.save(newUser);

        Optional<User> foundUserOpt = userRepository.findByUserId("testuser");

        assertTrue(foundUserOpt.isPresent());
        assertEquals("testuser", foundUserOpt.get().getUserId());
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 조회 테스트")
    void findByUserId_whenUserNotFound() throws DatabaseException {
        Optional<User> foundUserOpt = userRepository.findByUserId("ghost");
        assertFalse(foundUserOpt.isPresent());
    }
}