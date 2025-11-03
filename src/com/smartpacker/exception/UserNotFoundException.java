package com.smartpacker.exception;

/**
 * 데이터베이스나 저장소에서 특정 사용자를 찾을 수 없을 때 발생하는 예외입니다.
 */
public class UserNotFoundException extends Exception {
	
	private static final long serialVersionUID = 1L;

    /**
     * 기본 생성자. "사용자를 찾을 수 없습니다." 라는 기본 메시지를 사용합니다.
     */
    public UserNotFoundException() {
        super("해당 사용자를 찾을 수 없습니다.");
    }

    /**
     * 상세 메시지를 포함하는 생성자입니다.
     * @param message 예외에 대한 상세 설명
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}