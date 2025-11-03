package com.smartpacker.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    @DisplayName("Salt를 이용한 비밀번호 해싱 및 검증 성공 테스트")
    void testHashAndVerifyPassword_WithSalt_Success() {
        // Given: 원본 비밀번호와 새로 생성된 Salt
        String originalPassword = "mySecretPassword123!";
        String salt = PasswordHasher.generateSalt();
        assertNotNull(salt, "Salt가 정상적으로 생성되어야 합니다.");

        // When: 비밀번호를 Salt와 함께 해싱
        String hashedPassword = PasswordHasher.hashPassword(originalPassword, salt);

        // Then: 해싱 결과 검증
        assertNotNull(hashedPassword, "해시된 비밀번호는 null이 아니어야 합니다.");
        assertNotEquals(originalPassword, hashedPassword, "해시된 비밀번호는 원본과 달라야 합니다.");

        // Then: 검증 로직 테스트
        // 올바른 비밀번호와 올바른 Salt를 사용하면 검증에 성공해야 함
        assertTrue(PasswordHasher.verifyPassword(originalPassword, hashedPassword, salt), "올바른 비밀번호는 검증에 성공해야 합니다.");
        
        // 틀린 비밀번호를 사용하면 검증에 실패해야 함
        assertFalse(PasswordHasher.verifyPassword("wrongPassword", hashedPassword, salt), "틀린 비밀번호는 검증에 실패해야 합니다.");
        
        // 올바른 비밀번호라도, 틀린 Salt를 사용하면 검증에 실패해야 함
        String wrongSalt = PasswordHasher.generateSalt();
        assertFalse(PasswordHasher.verifyPassword(originalPassword, hashedPassword, wrongSalt), "틀린 Salt로는 검증에 실패해야 합니다.");
    }

    @Test
    @DisplayName("서로 다른 Salt는 동일한 비밀번호에 대해 서로 다른 해시를 생성해야 한다")
    void testDifferentSaltsProduceDifferentHashes() {
        // Given: 동일한 비밀번호와 서로 다른 두 개의 Salt
        String password = "consistentPassword";
        String salt1 = PasswordHasher.generateSalt();
        String salt2 = PasswordHasher.generateSalt();
        assertNotEquals(salt1, salt2, "테스트를 위해 두 Salt는 달라야 합니다.");

        // When: 각각의 Salt로 동일한 비밀번호를 해싱
        String hash1 = PasswordHasher.hashPassword(password, salt1);
        String hash2 = PasswordHasher.hashPassword(password, salt2);

        // Then: 생성된 두 해시 값은 서로 달라야 함
        assertNotEquals(hash1, hash2, "서로 다른 Salt는 동일한 비밀번호에 대해 서로 다른 해시 값을 생성해야 합니다.");
    }
}