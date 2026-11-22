package com.aegis.classes;

import org.bukkit.Material;
import java.util.List;

public enum GameClass {
    MINER("Шахтёр", Material.DIAMOND_PICKAXE,
            List.of("Кирка с автопереплавкой руд с 2-й минуты матча.",
                    "Повышенный шанс двойного дропа руды с уровнем.")),
    WARRIOR("Воин", Material.IRON_SWORD,
            List.of("Стартует с каменным мечом и железным нагрудником.",
                    "Увеличивает урон в ближнем бою с уровнем.")),
    ARCHER("Лучник", Material.BOW,
            List.of("Стартует с луком и стрелами.",
                    "Повышает урон из лука и шанс возврата стрел.")),
    ALCHEMIST("Алхимик", Material.BREWING_STAND,
            List.of("Стартует с зельями скорости и исцеления.",
                    "Усиливает длительность зелий с уровнем.")),
    SCOUT("Разведчик", Material.FEATHER,
            List.of("Постоянный бонус скорости передвижения.",
                    "Снижает урон от падения с уровнем.")),
    BLACKSMITH("Кузнец", Material.ANVIL,
            List.of("Стартует с наковальней и железными слитками.",
                    "Снижает стоимость починки и зачарований."));

    private final String displayName;
    private final Material icon;
    private final List<String> description;

    GameClass(String displayName, Material icon, List<String> description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public List<String> getDescription() { return description; }
}
