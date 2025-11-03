package com.smartpacker.repository;

import com.smartpacker.domain.user.User;
import com.smartpacker.exception.DatabaseException;
import java.util.Optional;

/**
 * 사용자 데이터에 접근하기 위한 인터페이스입니다.
 */
public interface UserRepository {
    /**
     * 초기 실행 시 데이터베이스 테이블을 생성합니다.
     * @throws DatabaseException DB 오류 발생 시
     */
    void setupDatabase() throws DatabaseException;

    /**
     * 새로운 사용자를 데이터베이스에 저장합니다.
     * @param user 저장할 사용자 객체
     * @throws DatabaseException DB 오류 발생 시
     */
    void save(User user) throws DatabaseException;

    /**
     * 사용자 ID로 사용자를 조회합니다.
     * @param userId 조회할 사용자 ID
     * @return 사용자를 찾으면 Optional<User> 객체를, 찾지 못하면 Optional.empty()를 반환
     * @throws DatabaseException DB 오류 발생 시
     */
    Optional<User> findByUserId(String userId) throws DatabaseException;
}