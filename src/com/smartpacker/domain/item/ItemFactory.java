package com.smartpacker.domain.item;

public class ItemFactory {
    /**
     * 카테고리 문자열을 기반으로 적절한 Item 구체 클래스의 인스턴스를 생성합니다.
     * @param name 아이템 이름
     * @param category 아이템 카테고리
     * @param quantity 수량
     * @return 생성된 Item 객체
     */
    public static Item create(String name, String category, int quantity) {
        switch (category) {
            case "필수품":
                return new EssentialItem(name, quantity);
            case "화장품":
                return new CosmeticItem(name, quantity);
            case "잡화":
                return new GeneralGoodsItem(name, quantity);
            case "엔터테인먼트":
                return new EntertainmentItem(name, quantity);
            case "업무":
                return new WorkItem(name, quantity);
            case "식품":
                return new FoodItem(name, quantity);
            case "의류":
                return new ClothingItem(name, quantity);
            case "전자기기":
                return new ElectronicItem(name, quantity);
            case "기타":
                return new EtcItem(name, quantity);
            default:
                // 알 수 없는 카테고리는 기타로 처리
                return new EtcItem(name, quantity);
        }
    }
}