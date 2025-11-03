package com.smartpacker.domain.item;

import java.util.Objects;

/**
 * 패킹 리스트에 포함될 모든 아이템의 기본 속성을 정의하는 추상 클래스입니다.
 * 모든 구체적인 아이템(e.g., 의류, 전자기기)은 이 클래스를 상속받아야 합니다.
 */
public abstract class Item {
    protected String name;      // 아이템 이름 (e.g., "반팔 티셔츠")
    protected String category;  // 아이템 카테고리 (e.g., "의류", "전자기기")
    protected int quantity;     // 수량

    public Item(String name, String category, int quantity) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
    }

    // 각 필드에 대한 Getter와 Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %d개", name, category, quantity);
    }

    // 객체의 동등성 비교를 위해 equals()와 hashCode()를 오버라이드합니다.
    // 아이템의 '이름'이 같으면 같은 아이템으로 취급합니다.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(name, item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
            
  }
