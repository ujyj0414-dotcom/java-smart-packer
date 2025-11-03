package com.smartpacker.config;

import java.util.Arrays;
import java.util.List;

/**
 * 애플리케이션의 모든 주요 설정값을 중앙에서 관리하는 클래스입니다.
 * final 키워드를 사용하여 외부에서 이 클래스를 상속할 수 없도록 하고,
 * private 생성자를 두어 객체 생성을 방지하여 순수한 유틸리티/상수 클래스로 사용합니다.
 */
public final class AppConfig {

    // 생성자를 private으로 선언하여 객체 생성을 막음
    private AppConfig() {}

    // --- 데이터베이스 설정 ---
    public static final String DB_URL = "jdbc:sqlite:smart_packer.db";

    // --- AI 추천 엔진 설정 ---
    public static final int MAX_AI_RECOMMENDATIONS = 5; // AI 추천 시 보여줄 최대 개수
    public static final List<String> ESSENTIAL_ITEMS = Arrays.asList(
        "여권", "지갑", "항공권", "스마트폰", "상비약"
    );

    // --- UI 및 공통 설정 ---
    // 카테고리 목록을 중앙에서 관리
    public static final List<String> CATEGORIES = Arrays.asList(
            "필수품", "의류", "전자기기", "화장품", "잡화", "식품", "업무", "엔터테인먼트", "기타"
    );
}