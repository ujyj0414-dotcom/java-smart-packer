package com.smartpacker.domain.user;

import com.smartpacker.exception.DatabaseException;
import com.smartpacker.exception.DuplicateUserException;
import com.smartpacker.exception.UserNotFoundException;
import com.smartpacker.exception.InvalidPasswordException; 

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface UserService {

    /**
     * 사용자 등록(회원가입)을 처리합니다.
     * @param userId 가입할 사용자 ID
     * @param password 가입할 사용자의 평문 비밀번호
     * @throws DuplicateUserException 이미 존재하는 사용자 ID일 경우
     * @throws DatabaseException 데이터베이스 오류 발생 시
     */
    void register(String userId, String password) throws DuplicateUserException, DatabaseException;

    /**
     * 사용자 로그인을 처리합니다.
     * @param userId 로그인할 사용자 ID
     * @param password 사용자가 입력한 평문 비밀번호
     * @return 로그인에 성공하면 해당 User 객체를 반환
     * @throws UserNotFoundException 사용자 ID가 존재하지 않을 경우
     * @throws IllegalArgumentException 비밀번호가 일치하지 않을 경우
     * @throws DatabaseException 데이터베이스 오류 발생 시
     */
    User login(String userId, String password) throws UserNotFoundException, InvalidPasswordException, DatabaseException;
}