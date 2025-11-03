package com.smartpacker.util;

import com.smartpacker.domain.item.Item;
import com.smartpacker.domain.item.ItemFactory;
import com.smartpacker.domain.packing.PackingList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 파일 읽기/쓰기를 담당하는 유틸리티 클래스입니다.
 */
public class FileIO {

    /**
     * PackingList 객체를 .txt 파일로 내보냅니다.
     * @param packingList 내보낼 PackingList 객체
     * @param filePath 저장할 파일 경로 (e.g., "my_trip.txt")
     * @throws IOException 파일 쓰기 중 오류 발생 시
     */
    public static void exportToTxt(PackingList packingList, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("========================================");
            writer.newLine();
            writer.write("  패킹 리스트: " + packingList.getListName());
            writer.newLine();
            writer.write("  여행 태그: #" + packingList.getTags().replace(",", " #"));
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
            writer.newLine();

            writer.write("[ 아이템 목록 ]");
            writer.newLine();
            for (Item item : packingList.getItems()) {
                writer.write(String.format("- %s (%s): %d개",
                        item.getName(), item.getCategory(), item.getQuantity()));
                writer.newLine();
            }
            writer.newLine();
            writer.write("생성일: " + packingList.getCreatedAt().toLocalDate());
            writer.newLine();
        }
    }

    /**
     * PackingList 객체를 Markdown(.md) 체크리스트 파일로 내보냅니다.
     * @param packingList 내보낼 PackingList 객체
     * @param filePath 저장할 파일 경로 (e.g., "my_trip.md")
     * @throws IOException 파일 쓰기 중 오류 발생 시
     */
    public static void exportToMarkdown(PackingList packingList, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("# 📋 패킹 리스트: " + packingList.getListName());
            writer.newLine();
            writer.newLine();

            writer.write("**여행 태그:** #" + packingList.getTags().replace(",", " #"));
            writer.newLine();
            writer.newLine();

            writer.write("## 아이템 목록");
            writer.newLine();
            for (Item item : packingList.getItems()) {
                writer.write(String.format("- [ ] %s (%s) - %d개",
                        item.getName(), item.getCategory(), item.getQuantity()));
                writer.newLine();
            }
        }
    }

    /**
     * 텍스트 파일로부터 아이템 목록을 읽어 '내 옷장'에 일괄 등록합니다.
     * 파일 형식: 아이템이름,카테고리,수량 (한 줄에 하나씩)
     * @param filePath 읽어올 파일 경로
     * @return 파싱된 Item 객체의 리스트
     * @throws IOException 파일 읽기 중 오류 발생 시
     * @throws IllegalArgumentException 파일 형식 오류 시
     */
    public static FileImportResult importFromTxt(String filePath) throws IOException {
        FileImportResult result = new FileImportResult();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    lineNumber++;
                    continue;
                }
                
                String[] parts = line.split(",");
                // 한 라인에서 오류가 발생해도 전체를 중단하지 않고, 해당 라인만 실패 처리
                try {
                    if (parts.length != 3) {
                        throw new IllegalArgumentException("형식 오류 (3개 필드 필요)");
                    }
                    
                    String name = parts[0].trim();
                    String category = parts[1].trim();
                    int quantity = Integer.parseInt(parts[2].trim());

                    if(name.isEmpty() || category.isEmpty() || quantity <= 0) {
                        throw new IllegalArgumentException("내용 오류 (빈 값 또는 0 이하 수량)");
                    }
                    
                    result.successItems.add(ItemFactory.create(name, category, quantity));
                } catch (Exception e) { // NumberFormatException, IllegalArgumentException 등 모두 처리
                    result.failedLines.add(String.format("%d번째 줄 '%s' (%s)", lineNumber, line, e.getMessage()));
                }
                lineNumber++;
            }
        }
        return result;
    }
    
   
}