package com.smartpacker;

import com.smartpacker.cli.ConsoleUI;
import com.smartpacker.cli.MainController;
import com.smartpacker.domain.packing.AnalysisEngine;
import com.smartpacker.domain.packing.PackingService;
import com.smartpacker.domain.packing.PackingServiceImpl;
import com.smartpacker.domain.user.MyClosetService;
import com.smartpacker.domain.user.MyClosetServiceImpl;
import com.smartpacker.domain.user.UserService;
import com.smartpacker.domain.user.UserServiceImpl;
import com.smartpacker.exception.DatabaseException;
import com.smartpacker.repository.MyClosetRepository;
import com.smartpacker.repository.MyClosetRepositoryImpl;
import com.smartpacker.repository.PackingListRepository;
import com.smartpacker.repository.PackingListRepositoryImpl;
import com.smartpacker.repository.UserRepository;
import com.smartpacker.repository.UserRepositoryImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Smart Packer CLI 애플리케이션의 시작점(Entry Point)입니다.
 * 모든 객체의 의존성을 생성하고 주입(DI)하는 역할을 담당합니다.
 */
public class Application {
	
	private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        
        // --- DB 초기화 모드 확인 ---
        boolean resetMode = (args.length > 0 && "reset-db".equals(args[0]));
        
        log.info("Smart Packer CLI v7.6 시스템을 초기화합니다...");
        
        try {
            // =================================================================
            // 1. 데이터 영속성 계층 (Repository) 객체 생성
            // =================================================================
            UserRepository userRepository = new UserRepositoryImpl();
            MyClosetRepository myClosetRepository = new MyClosetRepositoryImpl();
            PackingListRepository packingListRepository = new PackingListRepositoryImpl();

            // =================================================================
            // 2. 데이터베이스 초기 설정 (테이블 생성 및 데이터 초기화/시딩)
            // =================================================================
            userRepository.setupDatabase(); // 모든 테이블 구조가 없으면 생성

            if (resetMode) {
            	log.info("[Reset Mode] 사용자 정보를 제외한 모든 패킹 데이터를 초기화합니다...");
                myClosetRepository.deleteAllData();    // '내 옷장' 데이터 삭제
                packingListRepository.deleteAllData(); // '패킹 리스트' 데이터 삭제
            }

            packingListRepository.seedSharedLists(); // 공유 데이터가 없으면 새로 생성

            // =================================================================
            // 3. 비즈니스 로직 계층 (Service, Engine) 객체 생성 및 의존성 주입
            // =================================================================
            AnalysisEngine analysisEngine = new AnalysisEngine(packingListRepository);
            UserService userService = new UserServiceImpl(userRepository);
            MyClosetService myClosetService = new MyClosetServiceImpl(myClosetRepository);
            PackingService packingService = new PackingServiceImpl(packingListRepository, myClosetRepository, analysisEngine);

            // =================================================================
            // 4. 프레젠테이션 계층 (UI, Controller) 객체 생성 및 의존성 주입
            // =================================================================
            ConsoleUI consoleUI = new ConsoleUI();
            MainController mainController = new MainController(consoleUI, userService, packingService, myClosetService);

            // =================================================================
            // 5. 애플리케이션 실행
            // =================================================================
            mainController.run();

        } catch (DatabaseException e) {
        	log.error("데이터베이스 관련 오류로 프로그램을 시작할 수 없습니다.", e);
        } catch (Exception e) {
        	log.error("[CRITICAL] 알 수 없는 심각한 오류로 프로그램을 시작할 수 없습니다.", e);
        }
    }
}