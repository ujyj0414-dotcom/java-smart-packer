package com.smartpacker.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 아이템 이름을 기반으로 카테고리를 추천하는 유틸리티 클래스입니다.
 */
public class CategorySuggester {

    private static final Map<String, String[]> keywordMap = new HashMap<>();

    static {
        // '의류' 키워드 확장
        keywordMap.put("의류", new String[]{
            "셔츠", "바지", "티셔츠", "반팔", "긴팔", "자켓", "재킷", "코트", "가디건", "카디건", "양말", "속옷", "내복", "잠옷",
            "스웨터", "후드", "청바지", "반바지", "긴바지", "슬랙스", "블라우스", "원피스", "수영복", "래시가드",
            "등산복", "운동복", "트레이닝복", 
        });
        
        // '전자기기' 키워드 확장
        keywordMap.put("전자기기", new String[]{
            "충전기", "노트북", "랩탑", "맥북", "카메라", "보조배터리", "보조 배터리", "핸드폰", "휴대폰", "스마트폰",
            "고데기", "드라이기", "이어폰", "에어팟", "버즈", "헤드폰", "헤드셋", "멀티탭", "콘센트", "어댑터", "돼지코",
            "스피커", "태블릿", "아이패드", "갤럭시탭", "전자책", "리더기"
        });

        // '화장품' 키워드 확장
        keywordMap.put("화장품", new String[]{
            "로션", "스킨", "토너", "에센스", "세럼", "선크림", "썬크림", "자외선 차단제", "크림", "수분크림",
            "폼클렌징", "클렌징", "클렌저", "립밤", "화장품", "메이크업", "파우치", "파운데이션", "쿠션",
            "마스크팩", "샴푸", "린스", "컨디셔너", "바디워시", "치약", "칫솔", "가글"
        });

        // '필수품' 키워드 확장
        keywordMap.put("필수품", new String[]{
            "여권", "지갑", "항공권", "비행기표", "티켓", "신분증", "민증", "면허증", "비자", "상비약", "약"
        });

        // '잡화' 키워드 추가 (이전 버전에 없었다면 추가)
        keywordMap.put("잡화", new String[]{
            "모자", "선글라스", "안경", "렌즈", "우산", "양산", "가방", "백팩", "에코백", "목도리", "스카프",
            "장갑", "목베개", "안대", "귀마개", "수건", "타월", "물티슈", "휴지", "마스크", "슬리퍼"
        });
        
        // '식품' 키워드 확장
        keywordMap.put("식품", new String[]{
            "물", "생수", "간식", "음료수", "에너지바", "초콜릿", "사탕", "컵라면", "햇반"
        });

        // '업무' 키워드 확장
        keywordMap.put("업무", new String[]{
            "명함", "서류", "계약서", "노트", "필기구", "펜", "볼펜"
        });

        // '엔터테인먼트' 키워드 추가
        keywordMap.put("엔터테인먼트", new String[]{
            "책", "소설", "잡지", "게임기", "닌텐도", "스위치"
        });
    }


    /**
     * 아이템 이름을 분석하여 가장 적절한 카테고리를 추천합니다.
     * @param itemName 사용자가 입력한 아이템 이름
     * @return 추천되는 카테고리 문자열. 추천할 것이 없으면 null을 반환.
     */
    public static String suggest(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            return null;
        }
        
        String lowerItemName = itemName.toLowerCase();

        for (Map.Entry<String, String[]> entry : keywordMap.entrySet()) {
            String category = entry.getKey();
            String[] keywords = entry.getValue();
            
            if (Arrays.stream(keywords).anyMatch(lowerItemName::contains)) {
                return category; // 키워드가 포함된 첫 번째 카테고리를 반환
            }
        }

        return null; // 적절한 카테고리를 찾지 못한 경우
    }
}