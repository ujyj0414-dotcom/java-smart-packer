package com.smartpacker.domain.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordHasher {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH_BYTES = 16;

    /**
     * 보안에 안전한 새로운 Salt를 생성합니다.
     * @return 16진수 문자열로 인코딩된 Salt
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        random.nextBytes(salt);
        return bytesToHex(salt);
    }

    /**
     * 주어진 평문 비밀번호와 Salt를 사용하여 SHA-256 해시 값을 생성합니다.
     * @param passwordToHash 해싱할 평문 비밀번호
     * @param salt 해싱에 사용할 Salt
     * @return 16진수 문자열로 변환된 해시 값
     */
    public static String hashPassword(String passwordToHash, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            // 비밀번호와 Salt를 합쳐서 해싱
            String saltedPassword = passwordToHash + salt;
            byte[] hashedBytes = digest.digest(saltedPassword.getBytes());
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(HASH_ALGORITHM + " 해싱 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    /**
     * 입력된 평문 비밀번호와 저장된 해시 값/Salt를 비교하여 일치 여부를 확인합니다.
     * @param plainPassword 사용자가 입력한 평문 비밀번호
     * @param hashedPassword 데이터베이스에 저장된 해시된 비밀번호
     * @param salt 데이터베이스에 저장된 Salt
     * @return 비밀번호가 일치하면 true, 그렇지 않으면 false
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword, String salt) {
        // 사용자가 입력한 평문 비밀번호를 DB의 Salt를 사용하여 동일한 방식으로 해싱
        String newHash = hashPassword(plainPassword, salt);
        return newHash.equals(hashedPassword);
    }

    /**
     * 바이트 배열을 16진수 문자열로 변환하는 헬퍼 메소드입니다.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}