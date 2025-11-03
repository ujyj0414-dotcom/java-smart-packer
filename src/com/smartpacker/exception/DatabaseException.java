package com.smartpacker.exception;

/**
 * 데이터베이스 연동 중 발생하는 일반적인 오류에 대한 예외입니다.
 * 주로 SQLException을 감싸서(wrapping) 사용됩니다.
 */
public class DatabaseException extends Exception {
	
	private static final long serialVersionUID = 1L;

    /**
     * 상세 메시지를 포함하는 생성자입니다.
     * @param message 예외에 대한 상세 설명
     */
    public DatabaseException(String message) {
        super(message);
    }

    /**
     * 상세 메시지와 원인(cause) 예외를 포함하는 생성자입니다.
     * 이 생성자는 다른 예외(e.g., SQLException)를 이 예외의 원인으로 설정하여
     * 예외의 근본 원인을 추적할 수 있게 해줍니다.
     *
     * @param message 예외에 대한 상세 설명
     * @param cause 이 예외를 발생시킨 근본 원인 예외
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}