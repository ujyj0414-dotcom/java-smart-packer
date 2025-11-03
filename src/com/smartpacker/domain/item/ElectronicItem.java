package com.smartpacker.domain.item;

public class ElectronicItem extends Item {
    public ElectronicItem(String name, int quantity) {
        super(name, "전자기기", quantity);
    }
    // 필요 시 전압(voltage) 등 추가 속성 정의 가능
}