package com.smartpacker.repository;

import com.smartpacker.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC 연결 및 자원 해제를 관리하는 유틸리티 클래스입니다.
 */
public class JdbcManager {

    // 데이터베이스 연결을 생성하여 반환합니다.
    public static Connection getConnection() throws SQLException {
        // DatabaseConfig에 정의된 DB URL을 사용하여 Connection 객체를 생성합니다.
        return DriverManager.getConnection(AppConfig.DB_URL);
    }

    // 사용한 JDBC 자원들을 안전하게 닫습니다.
    // Connection, PreparedStatement, ResultSet 순서로 닫습니다. (생성의 역순)
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        // ResultSet 닫기
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // 로깅 라이브러리가 있다면 여기에 로그를 남기는 것이 좋습니다.
                System.err.println("[ERROR] ResultSet 닫기 실패: " + e.getMessage());
            }
        }

        // PreparedStatement 닫기
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                System.err.println("[ERROR] PreparedStatement 닫기 실패: " + e.getMessage());
            }
        }

        // Connection 닫기
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("[ERROR] Connection 닫기 실패: " + e.getMessage());
            }
        }
    }

    // ResultSet이 없는 경우(INSERT, UPDATE, DELETE 등)를 위한 오버로딩 메소드
    public static void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }
}