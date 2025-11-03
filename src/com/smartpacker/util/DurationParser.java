package com.smartpacker.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 기간 관련 문자열을 파싱하는 유틸리티 클래스입니다.
 */
public class DurationParser {

    /**
     * "3일", "일주일", "10박 11일" 등 다양한 기간 문자열에서 '일(day)' 수를 추론하여 반환합니다.
     * @param durationStr 기간 문자열
     * @return 추론된 일 수. 추론이 불가능하면 기본값 1을 반환.
     */
    public static int parseDays(String durationStr) {
        if (durationStr == null || durationStr.trim().isEmpty()) {
            return 1;
        }
        
        durationStr = durationStr.trim();

        // "일주일" or "1주" or "주" 포함 시 7일로 간주
        if (durationStr.contains("일주일") || durationStr.contains("1주")) {
            return 7;
        }
        if (durationStr.contains("2주")) {
            return 14;
        }

        // 정규표현식을 사용하여 숫자만 추출 (e.g., "10박 11일" -> 11, "3일" -> 3)
        // 가장 마지막에 나오는 숫자를 '일' 수로 간주
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(durationStr);
        
        String lastNumber = "";
        while (matcher.find()) {
            lastNumber = matcher.group();
        }

        if (!lastNumber.isEmpty()) {
            try {
                int days = Integer.parseInt(lastNumber);
                // "10박 11일" 같은 경우, '박' 수보다 '일' 수가 더 크므로 더 큰 값을 일 수로 간주
                if (durationStr.contains("박") && durationStr.contains("일")) {
                    // 간단한 구현에서는 마지막 숫자만 사용해도 충분
                }
                return Math.max(days, 1); // 최소 1일 보장
            } catch (NumberFormatException e) {
                // 숫자 변환 실패 시 무시
            }
        }

        return 1; // 아무것도 찾지 못하면 기본 1일
    }
}