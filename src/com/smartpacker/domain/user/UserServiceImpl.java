package com.smartpacker.domain.user;

import com.smartpacker.exception.DatabaseException;
import com.smartpacker.exception.DuplicateUserException;
import com.smartpacker.exception.UserNotFoundException;
import com.smartpacker.repository.UserRepository;
import com.smartpacker.exception.InvalidPasswordException;

/**
 * UserService의 구현 클래스입니다.
 */
public class UserServiceImpl implements UserService {

    // 의존성: UserService는 UserRepository에 의존합니다.
    private final UserRepository userRepository;

    /**
     * 생성자를 통해 외부에서 UserRepository 구현체를 주입받습니다. (Dependency Injection)
     * @param userRepository 사용할 UserRepository 객체
     */
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void register(String userId, String password) throws DuplicateUserException, DatabaseException {
        // 1. 비즈니스 규칙: ID가 이미 존재하는지 확인
        if (userRepository.findByUserId(userId).isPresent()) {
            throw new DuplicateUserException("'" + userId + "'는 이미 사용 중인 ID입니다.");
        }
        
        // 1. Salt 생성
        String salt = PasswordHasher.generateSalt();
        // 2. Salt를 사용하여 비밀번호 해싱
        String hashedPassword = PasswordHasher.hashPassword(password, salt);

        // 3. User 객체에 hash와 salt 모두 저장
        User newUser = new User(userId, hashedPassword, salt);
        userRepository.save(newUser);
    }

        

@Override
public User login(String userId, String password) throws UserNotFoundException, InvalidPasswordException, DatabaseException {
    User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException("'" + userId + "' 사용자를 찾을 수 없습니다."));

    // 비밀번호 검증 시 DB에서 가져온 user 객체의 Salt를 사용
    if (!PasswordHasher.verifyPassword(password, user.getPasswordHash(), user.getPasswordSalt())) {
        throw new InvalidPasswordException();
    }

    return user;
}
}