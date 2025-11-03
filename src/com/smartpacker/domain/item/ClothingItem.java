package com.smartpacker.domain.item;

/**
 * '의류' 카테고리의 아이템을 나타내는 구체적인 클래스입니다.
 * Item 클래스를 상속받습니다.
 */
public class ClothingItem extends Item {

    public ClothingItem(String name, int quantity) {
        // 부모 클래스의 생성자를 호출하며, 카테고리를 "의류"로 고정합니다.
        super(name, "의류", quantity);
    }

    // 필요하다면 의류에만 해당하는 필드(e.g., 소재, 색상)와 메소드를 추가할 수 있습니다.
}