package com.smartpacker.cli;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.smartpacker.domain.item.EtcItem;
import com.smartpacker.domain.item.Item;
import com.smartpacker.domain.packing.PackingList;
import com.smartpacker.config.AppConfig;

/**
 * 애플리케이션의 모든 콘솔 입출력을 담당하는 클래스입니다.
 * [E. UI/UX 가이드라인]을 총괄하여 적용합니다.
 */
public class ConsoleUI {


    private final Scanner scanner;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }
    
    // =================================================================
    //      입력 유틸리티 메소드 (안정적인 입력 처리를 위함)
    // =================================================================

    /**
     * 사용자로부터 문자열을 입력받습니다.
     * @param prompt 입력 프롬프트 메시지 (e.g., "> 아이디: ")
     * @return 사용자가 입력한 문자열
     */
    public String getInputString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
    
    /**
     * 사용자로부터 정수를 입력받습니다. 숫자가 아닐 경우 다시 입력하도록 반복합니다.
     * @param prompt 입력 프롬프트 메시지
     * @return 사용자가 입력한 정수
     */
    public int getInputInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine());
                return value;
            } catch (NumberFormatException e) {
                printErrorMessage("숫자만 입력해주세요.");
            }
        }
    }
    
    /**
     * 사용자로부터 Y/N 입력을 받아 boolean 값으로 반환합니다.
     * @param prompt Y/N 질문 메시지
     * @return 'y' 또는 'Y'를 입력하면 true, 'n' 또는 'N'을 입력하면 false
     */
    public boolean getYesOrNo(String prompt) {
        while (true) {
            String answer = getInputString(prompt).toLowerCase();
            if ("y".equals(answer)) {
                return true;
            } else if ("n".equals(answer)) {
                return false;
            } else {
                printErrorMessage("Y 또는 N으로만 입력해주세요.");
            }
        }
    }

    // =================================================================
    //      출력 포맷팅 메소드 (UI/UX 가이드라인 적용)
    // =================================================================

    /**
     * 정보 메시지를 출력합니다. (e.g., [INFO] ...)
     * @param message 출력할 메시지
     */
    public void printInfoMessage(String message) {
        System.out.println("[INFO] " + message);
    }
    
    /**
     * 성공 메시지를 출력합니다. (e.g., [SUCCESS] ...)
     * @param message 출력할 메시지
     */
    public void printSuccessMessage(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    /**
     * 에러 메시지를 출력합니다. (e.g., [ERROR] ...)
     * @param message 출력할 메시지
     */
    public void printErrorMessage(String message) {
        System.out.println("[ERROR] " + message);
    }
    
    /**
     * 화면을 지우는 효과를 줍니다. (콘솔 환경에 따라 동작이 다를 수 있음)
     */
    public void clearScreen() {
        // 간단하게 여러 줄을 출력하여 화면을 밀어내는 방식
        for (int i = 0; i < 50; ++i) System.out.println();
    }
    
    /**
     * 프로그램을 시작할 때 표시되는 환영 메시지입니다.
     */
    public void displayWelcome() {
        System.out.println("=================================================");
        System.out.println("  Smart Packer CLI v7.6 (데이터 기반 짐 싸기 엔진)");
        System.out.println("=================================================");
    }
    
    /**
     * 시작 메뉴(로그인/회원가입)를 출력하고 사용자 선택을 반환합니다.
     * @return 사용자가 선택한 메뉴 번호
     */
    public int displayStartMenu() {
        System.out.println("\n--- 시작 메뉴 ---");
        System.out.println("1. 로그인");
        System.out.println("2. 사용자 등록");
        System.out.println("0. 프로그램 종료");
        System.out.println("-----------------");
        return getInputInt("> 선택: ");
    }
    
    /**
     * 사용자로부터 비밀번호를 안전하게 입력받습니다.
     * 실제 터미널 환경에서는 마스킹 처리되며, IDE 환경에서는 일반 입력으로 동작합니다.
     * @param prompt 입력 프롬프트 메시지
     * @return 사용자가 입력한 비밀번호 문자열
     */
    public String getPasswordInput(String prompt) {
        java.io.Console console = System.console();
        
        // 1. 실제 콘솔(터미널) 환경인지 확인
        if (console != null) {
            // Console 객체가 존재하면, readPassword() 메소드를 사용하여 마스킹 입력 처리
            char[] passwordArray = console.readPassword(prompt);
            return new String(passwordArray);
        } else {
            // 2. IDE의 콘솔 등 System.console()이 null을 반환하는 환경
            // 기존의 Scanner를 사용하는 방식으로 대체 동작 (마스킹 X)
            // 사용자에게 이 상황을 알려주는 것이 좋습니다.
            System.out.println("\n[WARN] 보안 입력(마스킹)을 지원하지 않는 환경입니다. 비밀번호가 노출될 수 있습니다.");
            return getInputString(prompt);
        }
    }
    
    /**
     * 로그인 후의 메인 대시보드를 출력하고 사용자 선택을 반환합니다.
     * @param username 현재 로그인된 사용자의 ID
     * @return 사용자가 선택한 메뉴 번호
     */
    public int displayMainMenu(String username) {
        System.out.println("\n=================================================");
        System.out.println("  [ " + username + "님, 환영합니다! ]");
        System.out.println("-------------------------------------------------");
        System.out.println("              메인 대시보드");
        System.out.println("-------------------------------------------------");
        System.out.println("  1. 새 패킹 리스트 생성");
        System.out.println("  2. 내 패킹 리스트 조회");
        System.out.println("  3. 내 옷장 관리");
        System.out.println("---------------------------------");
        System.out.println("9. AI 학습 데이터 조회 (Admin)"); 
        System.out.println("0. 로그아웃");
        System.out.println("=================================================");
        return getInputInt("> 선택: ");
    }

    /**
     * [C-1] 여행 프로필 생성을 위한 질문을 표시합니다.
     * @return String 배열 [목적, 계절, 기간]
     */
    public String[] displayTravelProfileCreation() {
        System.out.println("\n--- [단계 1/3] 여행 프로필 생성 ---");
        String purpose = getInputString("> 여행 목적 (예: 휴양, 업무, 레저): ");
        String season = getInputString("> 계절 (예: 여름, 겨울): ");
        String duration = getInputString("> 기간 (예: 3일, 일주일): ");
        return new String[]{purpose, season, duration};
    }

    /**
     * [C-2] 패킹 리스트 시작 옵션을 표시하고 선택을 받습니다.
     * @return 1 (템플릿) 또는 2 (빈 리스트)
     */
    public int displayStartOptionSelection() {
        System.out.println("\n--- [단계 2/3] 시작 옵션 선택 ---");
        System.out.println("1. 기본 템플릿으로 시작");
        System.out.println("2. 빈 리스트에서 시작");
        int choice = 0;
        while (choice != 1 && choice != 2) {
            choice = getInputInt("> 선택: ");
            if (choice != 1 && choice != 2) {
                printErrorMessage("1 또는 2만 입력해주세요.");
            }
        }
        return choice;
    }

    /**
     * [C-3] 패킹 리스트 편집 화면을 표시합니다.
     * @param currentItems 현재 패킹 리스트에 담긴 아이템 목록
     */
    public void displayPackingListEditor(List<Item> currentItems) {
        System.out.println("\n--- [단계 3/3] 패킹 리스트 편집 ---");
        if (currentItems.isEmpty()) {
            System.out.println("  현재 리스트가 비어있습니다.");
        } else {
            System.out.println("  [ 현재 패킹 리스트 ]");
            for (int i = 0; i < currentItems.size(); i++) {
                System.out.printf("  %d. %s\n", i + 1, currentItems.get(i).toString());
            }
        }
        System.out.println("------------------------------------");
        System.out.println("  1. 아이템 추가");
        System.out.println("  2. 아이템 삭제");
        System.out.println("  3. [AI] 추천 받기 ✨");
        System.out.println("  0. 완료 및 저장");
        System.out.println("------------------------------------");
    }

    /**
     * 추천 결과를 보기 좋게 출력합니다.
     * @param recommendations 추천 문구가 담긴 리스트
     */
    public void displayRecommendations(List<String> recommendations) {
        System.out.println("\n--- ✨ AI 추천 엔진 분석 결과 ---");
        if (recommendations.isEmpty()) {
            System.out.println("  분석할 데이터가 부족하거나, 이미 완벽한 리스트입니다!");
        } else {
            for (int i = 0; i < recommendations.size(); i++) {
                System.out.printf("  %d. %s\n", i + 1, recommendations.get(i));
            }
        }
        System.out.println("--------------------------------");
    }

    /**
     * 추천 목록을 보여준 후, 사용자로부터 추가할 아이템 번호를 입력받습니다.
     * @return 사용자가 입력한 문자열 (e.g., "1,3", "2", "" 등)
     */
    public String getInputStringForRecommendation() {
        // 안내 멘트를 유지하면서, 입력도 받을 수 있도록 합니다.
        return getInputString("> 추가할 아이템 번호를 입력하세요 (여러 개는 쉼표(,)로, 없으면 Enter): ");
    }
    
    /**
     * 사용자의 모든 패킹 리스트를 목록 형태로 출력하고,
     * 관리할 리스트의 ID를 입력받아 반환합니다.
     */
    public int displayMyPackingListsAndGetChoice(List<PackingList> packingLists) {
        System.out.println("\n--- 내 패킹 리스트 조회 ---");
        
        if (packingLists.isEmpty()) {
            System.out.println("  생성된 패킹 리스트가 없습니다. 메인 메뉴에서 새로 만들어보세요!");
            System.out.println("------------------------------------");
            getInputString("> 확인했으면 Enter를 누르세요...");
            return 0;
        }

        for (PackingList list : packingLists) {
            System.out.println("------------------------------------");
            System.out.printf("  ID: %d | 이름: %s\n", list.getId(), list.getListName());
            System.out.printf("  태그: #%s | 공유: %s\n", list.getTags().replace(",", " #"), list.isShared() ? "Y" : "N");
            System.out.printf("  아이템: %d개\n", list.getItems().size());
        }
        System.out.println("------------------------------------");
        
        System.out.println("작업할 리스트의 ID를 선택하세요.");
        System.out.println("(상세 보기, 수정, 분석, 내보내기 작업을 할 수 있습니다.)");
        System.out.println("메인 메뉴로 돌아가려면 0을 입력하세요.");
        return getInputInt("> ID 선택: ");
    }
    
    /**
     * 선택된 패킹 리스트에 대해 수행할 작업을 선택하는 메뉴를 출력합니다.
     * @param listName 선택된 리스트의 이름
     * @return 사용자가 선택한 메뉴 번호
     */
    public int displayPackingListActionMenu(String listName) {
    	System.out.println("\n--- [" + listName + "] 작업 선택 ---");
        System.out.println("1. 리스트 상세 보기");
        System.out.println("2. 이 리스트 수정하기"); // 수정 메뉴
        System.out.println("3. 이 여행 분석하기 (패킹 리포트)");
        System.out.println("4. 파일로 내보내기");
        System.out.println("0. 뒤로 가기");
        System.out.println("---------------------------");
        return getInputInt("> 선택: ");
    }
    
    public int displayFinalActionMenu() {
        System.out.println("\n--- 최종 작업 선택 ---");
        System.out.println("1. 이대로 저장하기");
        System.out.println("2. 추가로 편집하기");
        System.out.println("0. 저장 취소하고 나가기");
        System.out.println("----------------------");
        return getInputInt("> 선택: ");
    }
    
    /**
     * 패킹 리스트 최종 점검 리포트를 출력합니다.
     * @param report 리포트 문장들이 담긴 리스트
     */
    public void displayFinalCheckReport(List<String> report) {
        System.out.println("\n--- 🧐 패킹 리스트 최종 점검 ---");
        for (String line : report) {
            System.out.println("  " + line);
        }
        System.out.println("---------------------------------");
    }
    
    /**
     * 하나의 패킹 리스트에 포함된 모든 아이템을 상세하게 출력합니다.
     * @param packingList 상세 조회할 PackingList 객체
     */
    public void displayPackingListDetail(PackingList packingList) {
        System.out.println("\n--- [" + packingList.getListName() + "] 상세 보기 ---");
        System.out.println("  ID: " + packingList.getId());
        System.out.println("  태그: #" + packingList.getTags().replace(",", " #"));
        System.out.println("  생성일: " + packingList.getCreatedAt().toLocalDate());
        System.out.println("  공유 여부: " + (packingList.isShared() ? "Y" : "N"));
        System.out.println("------------------------------------");
        System.out.println("  [ 아이템 목록 ]");
        if (packingList.getItems().isEmpty()) {
            System.out.println("    (비어 있음)");
        } else {
            for (Item item : packingList.getItems()) {
                System.out.printf("    - %s (%s) %d개\n", item.getName(), item.getCategory(), item.getQuantity());
            }
        }
        System.out.println("------------------------------------");
    }
    
    /**
     * '내 옷장 관리' 메뉴를 출력합니다.
     * @return 사용자가 선택한 메뉴 번호
     */
    public int displayMyClosetMenu() {
        System.out.println("\n--- 내 옷장 관리 ---");
        System.out.println("1. 내 옷장 모든 아이템 보기");
        System.out.println("2. 아이템 추가하기");
        System.out.println("3. 아이템 삭제하기");
        System.out.println("4. 내 옷장 통계 보기"); // 추가
        System.out.println("5. .txt 파일로 일괄 등록하기"); // 번호 변경
        System.out.println("0. 메인 대시보드로 돌아가기");
        System.out.println("--------------------");
        return getInputInt("> 선택: ");
    }
    
    /**
     * 파일로 내보낼 형식을 선택하는 메뉴를 출력하고, 사용자의 선택을 반환합니다.
     * @return 사용자가 선택한 메뉴 번호 (1: txt, 2: md, 0: 취소)
     */
    public int displayExportFormatChoice() {
        System.out.println("\n--- 내보낼 파일 형식 선택 ---");
        System.out.println("1. 일반 텍스트 (.txt)");
        System.out.println("2. 마크다운 체크리스트 (.md)");
        System.out.println("0. 취소");
        System.out.println("---------------------------");
        return getInputInt("> 선택: ");
    }

    /**
     * '내 옷장'의 모든 아이템을 출력합니다.
     * @param closetItems 아이템 이름(String)을 Key로, Item 객체를 Value로 갖는 Map
     */
    public void displayMyClosetItems(Map<String, Item> closetItems) {
        System.out.println("\n--- 내 옷장 아이템 목록 ---");
        if (closetItems.isEmpty()) {
            System.out.println("  내 옷장이 비어있습니다.");
            return;
        }
        
        // 카테고리별로 정렬해서 보여주면 더 보기 좋습니다.
        closetItems.values().stream()
            .sorted((item1, item2) -> item1.getCategory().compareTo(item2.getCategory()))
            .forEach(item -> System.out.printf("  [%s] %s - %d개\n",
                    item.getCategory(), item.getName(), item.getQuantity()));
        
        System.out.println("---------------------------");
        getInputString("> 확인했으면 Enter를 누르세요...");
    }
    
    /**
     * '내 옷장 통계'를 텍스트 기반 막대 그래프로 시각화하여 출력합니다.
     * @param stats 카테고리별 아이템 개수가 담긴 Map
     */
    public void displayClosetStatistics(Map<String, Long> stats) {
        System.out.println("\n--- 내 옷장 통계 ---");
        if (stats.isEmpty()) {
            System.out.println("  통계를 표시할 아이템이 내 옷장에 없습니다.");
            System.out.println("--------------------");
            getInputString("> 확인했으면 Enter를 누르세요...");
            return;
        }

        // 가장 긴 카테고리 이름의 길이를 찾아서 포맷을 맞춥니다.
        int maxCategoryNameLength = 0;
        for (String category : stats.keySet()) {
            if (category.length() > maxCategoryNameLength) {
                maxCategoryNameLength = category.length();
            }
        }
        
        // 가장 많은 아이템 개수를 찾아서 막대그래프의 최대 길이를 정합니다.
        long maxCount = 0;
        for (long count : stats.values()) {
            if (count > maxCount) {
                maxCount = count;
            }
        }
        final int MAX_BAR_LENGTH = 30; // 막대그래프 최대 길이 (칸)

        System.out.println("카테고리별 아이템 분포:");
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            String category = entry.getKey();
            long count = entry.getValue();
            
            // 개수에 비례하여 막대 길이 계산
            int barLength = (int) ((double) count / maxCount * MAX_BAR_LENGTH);
            if (barLength == 0 && count > 0) {
                barLength = 1; // 최소 1개의 아이템이 있으면 막대 길이 1 보장
            }
            
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < barLength; i++) {
                bar.append("▇");
            }

            // 포맷팅하여 출력
            String format = "  %-" + (maxCategoryNameLength + 2) + "s | %s (%d개)";
            System.out.printf(format, category, bar.toString(), count);
            System.out.println();
        }
        System.out.println("--------------------");
        getInputString("> 확인했으면 Enter를 누르세요...");
    }
    
    /**
     * 패킹 분석 리포트를 형식에 맞춰 출력합니다.
     * @param listName 리스트 이름
     * @param closetUtilization '내 옷장' 활용도 (0.0 ~ 1.0 사이)
     * @param missingFromCloset '내 옷장'에 없는 아이템 목록
     */
    public void displayPackingAnalysisReport(String listName, double closetUtilization, Set<String> missingFromCloset) {
        System.out.println("\n--- 📝 [" + listName + "] 패킹 분석 리포트 ---");
        System.out.printf("  '내 옷장' 아이템 활용도: %.1f%%\n", closetUtilization * 100);
        System.out.println("  - 이번 여행에 챙긴 짐의 " + String.format("%.0f%%", closetUtilization * 100) + "는 평소에 관리하던 '내 옷장' 아이템이었습니다.");
        System.out.println();

        if (missingFromCloset.isEmpty()) {
            System.out.println("  '내 옷장'에 없는 새로운 아이템은 없었습니다. 완벽한 관리네요!");
        } else {
            System.out.println("  [ '내 옷장'에 추가할 만한 아이템 ]");
            System.out.println("  - 이번 여행에서 유용했던 아래 아이템들은 아직 '내 옷장'에 없네요.");
            for (String itemName : missingFromCloset) {
                System.out.println("    - " + itemName);
            }
        }
        System.out.println("-------------------------------------------------");
    }
    
    /**
     * '내 옷장'에서 찾은 유사 아이템 목록을 선택지로 보여줍니다.
     * @param similarItems 유사 아이템 리스트
     * @param originalKeyword 사용자가 원래 입력했던 키워드
     * @return 사용자가 선택한 아이템 (새로 추가를 원하면 null 반환)
     */
    public Item displaySimilarItemSelection(List<Item> similarItems, String originalKeyword) {
        System.out.println("\n[INFO] '내 옷장'에 비슷한 아이템이 있습니다. 어떤 것을 챙기시겠어요?");
        for (int i = 0; i < similarItems.size(); i++) {
            System.out.printf("  %d. %s\n", i + 1, similarItems.get(i).toString());
        }
        System.out.println("--------------------------------------------------");
        System.out.printf("  %d. 그냥 '%s'(으)로 새로 추가하기\n", similarItems.size() + 1, originalKeyword);
        System.out.println("  0. 취소");

        int choice = getInputInt("> 선택: ");
        
        if (choice > 0 && choice <= similarItems.size()) {
            return similarItems.get(choice - 1); // 사용자가 선택한 기존 아이템 반환
        } else if (choice == similarItems.size() + 1) {
            return null; // '새로 추가'를 선택했음을 null로 알림
        } else {
            // 0번(취소) 또는 잘못된 번호 입력 시
        	 return new EtcItem("CANCEL", 0);// 취소를 알리는 특수 객체 (임의로 정의)
        }
    }
    
    /**
     * 아이템 추가 시, 정의된 카테고리 목록을 보여주고 사용자가 선택하게 합니다.
     * @return 사용자가 선택한 카테고리 문자열
     */
    public String displayCategorySelection() {
        System.out.println("  > 카테고리를 선택하세요:");
        for (int i = 0; i < AppConfig.CATEGORIES.size(); i++) {
            System.out.printf("    %d. %s\n", i + 1, AppConfig.CATEGORIES.get(i));
        }
        
        int choice = 0;
        while (choice < 1 || choice > AppConfig.CATEGORIES.size()) {
            choice = getInputInt("  > 선택: ");
            if (choice < 1 || choice > AppConfig.CATEGORIES.size()) {
                printErrorMessage("목록에 있는 번호만 입력해주세요.");
            }
        }
        return AppConfig.CATEGORIES.get(choice - 1);
    }
    
    /**
     * 아이템 추가 시, '스마트 카테고리' 추천을 먼저 보여주고 사용자의 확인을 받습니다.
     * @param suggestedCategory 시스템이 추천하는 카테고리
     * @return 사용자가 추천을 수락하면 true, 거절하면 false
     */
    public boolean confirmSuggestedCategory(String suggestedCategory) {
        String prompt = String.format("[SMART] 추천 카테고리: '%s' (맞으면 y/아니면 n): ", suggestedCategory);
        return getYesOrNo(prompt);
    }

	
}