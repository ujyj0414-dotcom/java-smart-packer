package com.smartpacker.cli;

import com.smartpacker.domain.item.Item;
import com.smartpacker.domain.item.ItemFactory;
import com.smartpacker.domain.packing.PackingList;
import com.smartpacker.domain.packing.PackingService;
import com.smartpacker.domain.packing.PackingService.PackingAnalysisResult;
import com.smartpacker.domain.user.MyClosetService;
import com.smartpacker.domain.user.User;
import com.smartpacker.domain.user.UserService;
import com.smartpacker.exception.DatabaseException;
import com.smartpacker.exception.DuplicateUserException;
import com.smartpacker.exception.UserNotFoundException;
import com.smartpacker.util.CategorySuggester;
import com.smartpacker.util.DurationParser;
import com.smartpacker.util.FileIO;
import com.smartpacker.util.FileImportResult;
import com.smartpacker.exception.InvalidPasswordException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 사용자 입력을 받아 Service 계층에 처리를 위임하고,
 * 그 결과를 ConsoleUI를 통해 출력하도록 제어하는 메인 컨트롤러입니다.
 */
public class MainController {
	
	private static final Logger log = LoggerFactory.getLogger(MainController.class);
    private final ConsoleUI consoleUI;
    private final UserService userService;
    private final PackingService packingService;
    private final MyClosetService myClosetService;
    private User loggedInUser = null;

    public MainController(ConsoleUI consoleUI, UserService userService, PackingService packingService, MyClosetService myClosetService) {
        this.consoleUI = consoleUI;
        this.userService = userService;
        this.packingService = packingService;
        this.myClosetService = myClosetService;
    }

    public void run() {
        consoleUI.displayWelcome();
        boolean isRunning = true;
        while (isRunning) {
            if (isLoggedIn()) {
                handleMainMenu();
            } else {
                isRunning = handleStartMenu();
            }
        }
        log.info("애플리케이션 메인 루프를 종료합니다.");
    }

    private boolean handleStartMenu() {
        int choice = consoleUI.displayStartMenu();
        switch (choice) {
            case 1: login(); break;
            case 2: register(); break;
            case 0: exitProgram(); return false;
            default: consoleUI.printErrorMessage("잘못된 메뉴 번호입니다."); break;
        }
        return true;
    }

    private void handleMainMenu() {
        int choice = consoleUI.displayMainMenu(loggedInUser.getUserId());
        switch (choice) {
            case 1: createPackingListProcess(); break;
            case 2: showMyPackingLists(); break;
            case 3: manageMyCloset(); break;
            case 9: showAllSharedLists(); break;
            case 0: logout(); break;
            default: consoleUI.printErrorMessage("잘못된 메뉴 번호입니다."); break;
        }
    }

    // =================================================================
    //      User Management
    // =================================================================
    private void register() {
        log.info("사용자 등록 절차 시작."); // 내부 로그
        String userId = consoleUI.getInputString("> 아이디: ");
        String password = consoleUI.getPasswordInput("> 비밀번호: ");
        try {
            userService.register(userId, password);
            consoleUI.printSuccessMessage("사용자 등록에 성공했습니다! 이제 로그인해주세요.");
            log.info("사용자 등록 성공: {}", userId); // 성공 로그 (파라미터 사용)
        } catch (DuplicateUserException e) {
            consoleUI.printErrorMessage(e.getMessage());
            log.warn("사용자 등록 실패 (ID 중복): {}", userId); // 경고 로그
        } catch (DatabaseException e) {
            consoleUI.printErrorMessage(e.getMessage());
            log.error("사용자 등록 중 DB 오류 발생", e); // 에러 로그 (스택 트레이스 포함)
        }
    }

    private void login() {
        log.info("사용자 로그인 시도."); // 내부 로그
        String userId = consoleUI.getInputString("> 아이디: ");
        String password = consoleUI.getPasswordInput("> 비밀번호: ");
        try {
            this.loggedInUser = userService.login(userId, password);
            consoleUI.clearScreen();
            consoleUI.printSuccessMessage(loggedInUser.getUserId() + "님, 환영합니다!");
            log.info("사용자 로그인 성공: {}", userId);
        } catch (UserNotFoundException | InvalidPasswordException e) {
            consoleUI.printErrorMessage(e.getMessage());
            log.warn("사용자 로그인 실패 (사용자 없음 또는 비밀번호 오류): {}", userId);
        } catch (DatabaseException e) {
            consoleUI.printErrorMessage("로그인 처리 중 오류가 발생했습니다.");
            log.error("로그인 중 DB 오류 발생", e);
        }
    }

    private void logout() {
        log.info("사용자 로그아웃: {}", loggedInUser.getUserId());
        consoleUI.printInfoMessage(loggedInUser.getUserId() + "님이 로그아웃하셨습니다.");
        this.loggedInUser = null;
    }

    private void exitProgram() {
        consoleUI.printInfoMessage("Smart Packer CLI를 종료합니다.");
        log.info("사용자에 의해 프로그램이 종료됩니다.");
        System.exit(0);
    }
    
    /**
     * [DEV] 기능: 모든 공유 리스트 (AI 학습 데이터)를 조회하여 출력합니다.
     */
    private void showAllSharedLists() {
        log.info("[DEV] 모든 공유 리스트 조회 기능 실행."); // 로그 추가
        consoleUI.printInfoMessage("\n--- [DEV] 모든 공유 리스트 (AI 학습 데이터) ---");
        
        try {
            // 2단계에서 만든 서비스 메소드 호출
            List<PackingList> sharedLists = packingService.getAllSharedLists();
            
            if (sharedLists.isEmpty()) {
                consoleUI.printInfoMessage("현재 공유된 패킹 리스트가 없습니다.");
                consoleUI.printInfoMessage("----------------------------------------------");
                return;
            }

            // 님의 PackingList.java에 있는 getter들을 기반으로 출력
            for (PackingList list : sharedLists) {
                String tags = (list.getTags() == null || list.getTags().isEmpty()) ? "없음" : list.getTags();
                
                consoleUI.printInfoMessage(String.format(
                    "[%d] \"%s\" (작성자: %s, 태그: %s, 아이템 %d개)",
                    list.getId(),
                    list.getListName(),
                    list.getUserId(),
                    tags,
                    list.getItems().size() // list.getItems()는 List<Item>입니다.
                ));
            }
            consoleUI.printInfoMessage("----------------------------------------------");
            consoleUI.printInfoMessage(sharedLists.size() + "개의 공유 리스트를 조회했습니다.");

        } catch (DatabaseException e) {
            // 님의 기존 예외 처리 방식을 그대로 따릅니다.
            log.error("공유 리스트 조회 중 DB 오류", e);
            consoleUI.printErrorMessage("데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        // 메뉴 루프로 돌아가기 전 대기
        consoleUI.getInputString("\n> 확인했으면 Enter를 누르세요...");
    }
    
    // =================================================================
    //      Packing List Management
    // =================================================================
    private void createPackingListProcess() {
        String[] profile = consoleUI.displayTravelProfileCreation();
        String tags = String.join(",", profile);

        int startOption = consoleUI.displayStartOptionSelection();
        List<Item> currentItems = new ArrayList<>();
        if (startOption == 1) {
            consoleUI.printInfoMessage("여행 프로필에 맞는 기본 템플릿을 구성합니다...");
            int days = DurationParser.parseDays(profile[2]);
            consoleUI.printInfoMessage("예상 여행 기간: " + days + "일");

            currentItems.add(ItemFactory.create("여권", "필수품", 1));
            currentItems.add(ItemFactory.create("지갑", "필수품", 1));
            currentItems.add(ItemFactory.create("스마트폰 충전기", "전자기기", 1));

            if (tags.contains("겨울") || tags.contains("추운")) {
                currentItems.add(ItemFactory.create("두꺼운 외투", "의류", 1));
                currentItems.add(ItemFactory.create("히트텍", "의류", Math.max(1, (int) Math.ceil(days / 2.0))));
            } else if (tags.contains("여름") || tags.contains("더운")) {
                currentItems.add(ItemFactory.create("반팔 티셔츠", "의류", Math.max(1, days + 1)));
                currentItems.add(ItemFactory.create("선크림", "화장품", 1));
            }
            if (tags.contains("업무") || tags.contains("출장")) {
                currentItems.add(ItemFactory.create("노트북", "전자기기", 1));
            }
        }

        List<String> lastRecommendations = new ArrayList<>();

        while (true) {
            editPackingList(currentItems, lastRecommendations, profile);

            if (!lastRecommendations.isEmpty() && !currentItems.isEmpty()) {
                List<String> report = packingService.generateFinalCheckReport(
                    new PackingList(loggedInUser.getUserId(), "", "", currentItems), lastRecommendations);
                consoleUI.displayFinalCheckReport(report);
            }

            int finalChoice = consoleUI.displayFinalActionMenu();
            if (finalChoice == 1) {
                break;
            } else if (finalChoice == 2) {
                consoleUI.printInfoMessage("계속해서 리스트를 편집합니다...");
            } else {
                consoleUI.printInfoMessage("리스트 저장을 취소했습니다.");
                return;
            }
        }
        
        if (currentItems.isEmpty()) {
            consoleUI.printInfoMessage("리스트에 아이템이 없어 저장하지 않았습니다.");
            return;
        }

        String listName = consoleUI.getInputString("> 저장할 리스트 이름: ");
        try {
            PackingList savedList = packingService.createPackingList(loggedInUser.getUserId(), listName, tags, currentItems);
            consoleUI.printSuccessMessage("'" + listName + "' 리스트를 성공적으로 저장했습니다!");

            if (consoleUI.getYesOrNo("> 이 리스트를 다른 사용자들의 추천 데이터로 익명 공유하시겠습니까? (y/n): ")) {
                packingService.sharePackingList(savedList.getId(), true);
                consoleUI.printInfoMessage("소중한 데이터 공유에 감사드립니다!");
            }
        } catch (DatabaseException e) {
            consoleUI.printErrorMessage("리스트 저장 중 오류 발생: " + e.getMessage());
        }
    }

    private void showMyPackingLists() {
        try {
            List<PackingList> myLists = packingService.getMyPackingLists(loggedInUser.getUserId());
            int choiceId = consoleUI.displayMyPackingListsAndGetChoice(myLists);
            if (choiceId == 0) return;

            PackingList selectedList = myLists.stream().filter(list -> list.getId() == choiceId).findFirst().orElse(null);
            if (selectedList == null) {
                consoleUI.printErrorMessage("잘못된 ID입니다.");
                return;
            }

            boolean isActionMenu = true;
            while (isActionMenu) {
                int actionChoice = consoleUI.displayPackingListActionMenu(selectedList.getListName());
                switch (actionChoice) {
                    case 1:
                        consoleUI.displayPackingListDetail(selectedList);
                        consoleUI.getInputString("> 확인했으면 Enter를 누르세요...");
                        break;
                    case 2:
                        consoleUI.printInfoMessage("'" + selectedList.getListName() + "' 리스트 수정을 시작합니다.");
                        List<Item> itemsToEdit = new ArrayList<>(selectedList.getItems());
                        editPackingList(itemsToEdit, new ArrayList<>(), selectedList.getTags().split(","));
                        selectedList.setItems(itemsToEdit);
                        packingService.updatePackingList(selectedList);
                        consoleUI.printSuccessMessage("'" + selectedList.getListName() + "' 리스트를 성공적으로 수정했습니다.");
                        isActionMenu = false; // 수정 후에는 메뉴 선택 화면으로 빠져나감
                        break;
                    case 3:
                        PackingAnalysisResult result = packingService.analyzePackingList(selectedList, loggedInUser.getUserId());
                        consoleUI.displayPackingAnalysisReport(selectedList.getListName(), result.closetUtilization, result.missingFromCloset);
                        if (!result.missingFromCloset.isEmpty() && consoleUI.getYesOrNo("> 위 아이템들을 '내 옷장'에 추가하시겠습니까? (y/n): ")) {
                            for (String itemName : result.missingFromCloset) {
                                myClosetService.addItemToMyCloset(loggedInUser.getUserId(), ItemFactory.create(itemName, "기타", 1));
                            }
                            consoleUI.printSuccessMessage(result.missingFromCloset.size() + "개의 아이템을 '내 옷장'에 추가했습니다.");
                        }
                        consoleUI.getInputString("> 확인했으면 Enter를 누르세요...");
                        break;
                    case 4:
                        handleExport(selectedList);
                        break;
                    case 0:
                        isActionMenu = false;
                        break;
                    default:
                        consoleUI.printErrorMessage("잘못된 메뉴 번호입니다.");
                }
            }
        } catch (DatabaseException e) {
            consoleUI.printErrorMessage("패킹 리스트 작업 중 오류 발생: " + e.getMessage());
        }
    }

    // =================================================================
    //      My Closet Management
    // =================================================================
    private void manageMyCloset() {
        boolean isManaging = true;
        while(isManaging) {
            int choice = consoleUI.displayMyClosetMenu();
            switch (choice) {
                case 1:
                    try {
                        consoleUI.displayMyClosetItems(myClosetService.getMyClosetItems(loggedInUser.getUserId()));
                    } catch (DatabaseException e) { consoleUI.printErrorMessage("아이템 조회 중 오류: " + e.getMessage()); }
                    break;
                case 2:
                    try {
                        String name = consoleUI.getInputString("> 아이템 이름: ");
                        String category = getCategoryForItem(name);
                        int quantity = consoleUI.getInputInt("> 수량: ");
                        myClosetService.addItemToMyCloset(loggedInUser.getUserId(), ItemFactory.create(name, category, quantity));
                        consoleUI.printSuccessMessage("'" + name + "'을(를) 내 옷장에 추가했습니다.");
                    } catch (DatabaseException e) { consoleUI.printErrorMessage("아이템 추가 중 오류: " + e.getMessage()); }
                    break;
                case 3:
                    try {
                        String nameToDelete = consoleUI.getInputString("> 삭제할 아이템 이름: ");
                        if (myClosetService.removeItemFromMyCloset(loggedInUser.getUserId(), nameToDelete)) {
                            consoleUI.printSuccessMessage("'" + nameToDelete + "'을(를) 삭제했습니다.");
                        } else { consoleUI.printErrorMessage("해당 아이템을 찾을 수 없습니다."); }
                    } catch (DatabaseException e) { consoleUI.printErrorMessage("아이템 삭제 중 오류: " + e.getMessage()); }
                    break;
                case 4:
                    try {
                        consoleUI.displayClosetStatistics(myClosetService.getMyClosetStatistics(loggedInUser.getUserId()));
                    } catch (DatabaseException e) { consoleUI.printErrorMessage("통계 조회 중 오류: " + e.getMessage()); }
                    break;
                case 5:
                    try {
                        String filePath = consoleUI.getInputString("> 파일 경로: ");
                        FileImportResult result = myClosetService.addItemsToMyClosetFromFile(loggedInUser.getUserId(), filePath);
                        consoleUI.printSuccessMessage(result.successItems.size() + "개의 아이템을 추가했습니다.");
                        if (!result.failedLines.isEmpty()) {
                            consoleUI.printErrorMessage(result.failedLines.size() + "개 라인에서 오류 발생:");
                            result.failedLines.forEach(line -> System.out.println("  - " + line));
                        }
                    } catch (IOException | DatabaseException e) { consoleUI.printErrorMessage("파일 처리 중 오류: " + e.getMessage()); }
                    break;
                case 0: isManaging = false; break;
                default: consoleUI.printErrorMessage("잘못된 메뉴 번호입니다.");
            }
        }
    }

    // =================================================================
    //      Helper Methods
    // =================================================================
    private void editPackingList(List<Item> currentItems, List<String> lastRecommendations, String[] profile) {
        boolean isEditing = true;
        while (isEditing) {
            consoleUI.clearScreen();
            consoleUI.displayPackingListEditor(currentItems);
            int editChoice = consoleUI.getInputInt("> 편집 메뉴 선택: ");

            switch (editChoice) {
                case 1:
                    String name = consoleUI.getInputString("  > 추가할 아이템 이름: ");
                    List<Item> similarItems = myClosetService.findSimilarItemsInMyCloset(loggedInUser.getUserId(), name);
                    Item itemToAdd = null;
                    if (!similarItems.isEmpty()) {
                        Item selected = consoleUI.displaySimilarItemSelection(similarItems, name);
                        if (selected != null && "CANCEL".equals(selected.getName())) {
                            consoleUI.printInfoMessage("아이템 추가를 취소했습니다.");
                            break;
                        }
                        if (selected != null) itemToAdd = selected;
                    }
                    if (itemToAdd == null) {
                        String category = getCategoryForItem(name);
                        int quantity = consoleUI.getInputInt("  > 수량: ");
                        itemToAdd = ItemFactory.create(name, category, quantity);
                    } else {
                        int quantity = consoleUI.getInputInt("  > 수량 (현재 옷장에 " + itemToAdd.getQuantity() + "개 보유): ");
                        itemToAdd.setQuantity(quantity);
                    }
                    currentItems.add(itemToAdd);
                    consoleUI.printSuccessMessage("'" + itemToAdd.getName() + "' 아이템을 추가했습니다.");
                    consoleUI.getInputString("\n> 계속하려면 Enter를 누르세요...");
                    break;
                case 2:
                    if (currentItems.isEmpty()) {
                        consoleUI.printErrorMessage("삭제할 아이템이 없습니다.");
                    } else {
                        int indexToDelete = consoleUI.getInputInt("  > 삭제할 아이템 번호: ");
                        if (indexToDelete > 0 && indexToDelete <= currentItems.size()) {
                            currentItems.remove(indexToDelete - 1);
                        } else { consoleUI.printErrorMessage("잘못된 번호입니다."); }
                    }
                    consoleUI.getInputString("\n> 계속하려면 Enter를 누르세요...");
                    break;
                case 3:
                    try {
                        consoleUI.printInfoMessage("AI 추천 엔진을 가동합니다...");
                        lastRecommendations.clear();
                        lastRecommendations.addAll(packingService.getAiRecommendations(loggedInUser.getUserId(), profile, currentItems));
                        consoleUI.displayRecommendations(lastRecommendations);

                        if (!lastRecommendations.isEmpty()) {
                            String choice = consoleUI.getInputStringForRecommendation();
                            if (choice != null && !choice.trim().isEmpty()) {
                                for (String c : choice.split(",")) {
                                    try {
                                        int index = Integer.parseInt(c.trim()) - 1;
                                        if (index >= 0 && index < lastRecommendations.size()) {
                                            String itemName = parseItemNameFromRecommendation(lastRecommendations.get(index));
                                            if (currentItems.stream().anyMatch(item -> item.getName().equalsIgnoreCase(itemName))) {
                                                consoleUI.printErrorMessage("'" + itemName + "'은(는) 이미 리스트에 있습니다.");
                                                continue;
                                            }
                                            currentItems.add(ItemFactory.create(itemName, getCategoryForItem(itemName), 1));
                                            consoleUI.printSuccessMessage("'" + itemName + "'을(를) 리스트에 추가했습니다.");
                                        } else { consoleUI.printErrorMessage("'" + c + "'은(는) 잘못된 번호입니다."); }
                                    } catch (NumberFormatException e) { consoleUI.printErrorMessage("'" + c + "'은(는) 올바른 숫자가 아닙니다."); }
                                }
                            }
                        }
                    } catch (DatabaseException e) { consoleUI.printErrorMessage("추천 데이터 로딩 중 오류: " + e.getMessage()); }
                    consoleUI.getInputString("\n> 편집 메뉴로 돌아가려면 Enter를 누르세요...");
                    break;
                case 0:
                    isEditing = false;
                    break;
                default:
                    consoleUI.printErrorMessage("잘못된 메뉴 번호입니다.");
                    consoleUI.getInputString("\n> 계속하려면 Enter를 누르세요...");
                    break;
            }
        }
    }

    private String getCategoryForItem(String itemName) {
        String suggestedCategory = CategorySuggester.suggest(itemName);
        if (suggestedCategory != null && consoleUI.confirmSuggestedCategory(suggestedCategory)) {
            return suggestedCategory;
        } else {
            return consoleUI.displayCategorySelection();
        }
    }

    private String parseItemNameFromRecommendation(String rec) {
        String temp = rec.substring(rec.indexOf("]") + 1).trim();
        return temp.contains("(") ? temp.substring(0, temp.indexOf("(")).trim() : temp;
    }
    
    private void handleExport(PackingList list) {
        int formatChoice = consoleUI.displayExportFormatChoice();
        if (formatChoice == 0) return;
        String fileNameBase = list.getListName().replaceAll("\\s+", "_") + "_" + list.getId();
        try {
            if (formatChoice == 1) {
                FileIO.exportToTxt(list, fileNameBase + ".txt");
            } else if (formatChoice == 2) {
                FileIO.exportToMarkdown(list, fileNameBase + ".md");
            } else { consoleUI.printErrorMessage("잘못된 형식입니다."); return; }
            consoleUI.printSuccessMessage("'" + fileNameBase + (formatChoice == 1 ? ".txt" : ".md") + "' 파일로 내보내기가 완료되었습니다!");
        } catch (IOException e) { consoleUI.printErrorMessage("파일 저장 중 오류: " + e.getMessage()); }
    }

    private boolean isLoggedIn() {
        return this.loggedInUser != null;
    }

    
}