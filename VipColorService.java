package com.aegis.status;

import java.util.LinkedHashMap;
import java.util.Map;

public class VipColorService {

    public record BadgeStyle(String bgHex, String textHex, String russianName) {}

    private final Map<String, BadgeStyle> colors = new LinkedHashMap<>();

    public VipColorService() {
        colors.put("white",   new BadgeStyle("#ffffff", "#000000", "Белый"));
        colors.put("red",     new BadgeStyle("#dc2626", "#ffffff", "Красный"));
        colors.put("gray",    new BadgeStyle("#9ca3af", "#ffffff", "Серый"));
        colors.put("black",   new BadgeStyle("#111827", "#ffffff", "Чёрный"));
        colors.put("purple",  new BadgeStyle("#a855f7", "#ffffff", "Фиолетовый"));
        colors.put("pink",    new BadgeStyle("#e879f9", "#ffffff", "Розовый"));
        colors.put("lime",    new BadgeStyle("#84cc16", "#ffffff", "Салатовый"));
        colors.put("yellow",  new BadgeStyle("#eab308", "#ffffff", "Жёлтый"));
        colors.put("cherry",  new BadgeStyle("#be185d", "#ffffff", "Вишнёво-красный"));
        colors.put("magenta", new BadgeStyle("#c026d3", "#ffffff", "Пурпурный"));
    }

    public BadgeStyle getStyle(String key) {
        return colors.getOrDefault(key.toLowerCase(), colors.get("purple"));
    }

    public boolean exists(String key) {
        return colors.containsKey(key.toLowerCase());
    }

    public Map<String, BadgeStyle> getAll() {
        return colors;
    }
}
