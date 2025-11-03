package com.smartpacker.domain.packing;

import com.smartpacker.domain.item.Item;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 패킹 리스트 정보를 담는 도메인 모델 클래스입니다.
 */
public class PackingList {
    private long id;
    private String userId;
    private String listName;
    private String tags;
    private List<Item> items; // DB에는 JSON 문자열로 저장되지만, 객체에서는 List<Item>으로 다룹니다.
    private boolean isShared;
    private LocalDateTime createdAt;

    // 전체 필드를 받는 생성자 (DB에서 조회 시 사용)
    public PackingList(long id, String userId, String listName, String tags, List<Item> items, boolean isShared, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.listName = listName;
        this.tags = tags;
        this.items = items;
        this.isShared = isShared;
        this.createdAt = createdAt;
    }

    // 새로 생성할 때 사용하는 생성자 (id, createdAt은 DB에서 자동 생성)
    public PackingList(String userId, String listName, String tags, List<Item> items) {
        this.userId = userId;
        this.listName = listName;
        this.tags = tags;
        this.items = items;
        this.isShared = false; // 기본값은 false
    }

    // Getters
    public long getId() { return id; }
    public String getUserId() { return userId; }
    public String getListName() { return listName; }
    public String getTags() { return tags; }
    public List<Item> getItems() { return items; }
    public boolean isShared() { return isShared; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters (필요한 경우)
    public void setShared(boolean shared) { isShared = shared; }
    
    /**
     * 이 패킹 리스트의 아이템 목록을 새로운 리스트로 교체합니다. (수정 기능을 위함)
     * @param items 새로운 아이템 리스트
     */
    public void setItems(List<Item> items) { //  <-- 이 메소드를 추가!
        this.items = items;
    }
}
