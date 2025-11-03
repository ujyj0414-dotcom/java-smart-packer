package com.smartpacker.domain.user;

/**
 * 사용자 정보를 담는 도메인 모델 클래스입니다.
 */
public class User {
    private String userId;
    private String passwordHash; // 실제 비밀번호가 아닌, 해싱된 값을 저장합니다.
    private String passwordSalt;

    /**
     * 새로운 생성자: userId, passwordHash, passwordSalt를 모두 받습니다.
     * @param userId 사용자 ID
     * @param passwordHash 해싱된 비밀번호
     * @param passwordSalt 비밀번호 해싱에 사용된 Salt
     */
    public User(String userId, String passwordHash, String passwordSalt) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    // 각 필드에 대한 Getter
    public String getUserId() {
        return userId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
    
    public String getPasswordSalt() { 
    	return passwordSalt; 
    	}

    // toString() 메소드는 보안상 해시나 솔트 값을 출력하지 않는 것이 좋습니다.
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                '}';
    }
}