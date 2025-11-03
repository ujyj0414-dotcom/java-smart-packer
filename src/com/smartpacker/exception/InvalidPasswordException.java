package com.smartpacker.exception;

/**
 * 로그인 시 비밀번호가 일치하지 않을 때 발생하는 예외입니다.
 */
public class InvalidPasswordException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }

    public InvalidPasswordException(String message) {
        super(message);
    }
}