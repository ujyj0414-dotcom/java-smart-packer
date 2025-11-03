package com.smartpacker.exception;

/**
 * 사용자 등록 시, 이미 존재하는 사용자 ID로 시도했을 때 발생하는 예외입니다.
 */
public class DuplicateUserException extends Exception {
	
	private static final long serialVersionUID = 1L;

    /**
     * 기본 생성자. "이미 존재하는 사용자 ID입니다." 라는 기본 메시지를 사용합니다.
     */
    public DuplicateUserException() {
        super("이미 존재하는 사용자 ID입니다.");
    }

    /**
     * 상세 메시지를 포함하는 생성자입니다.
     * @param message 예외에 대한 상세 설명
     */
    public DuplicateUserException(String message) {
        super(message);
    }
}