package com.aegis.match;

public enum MatchPhase {
    WAITING("<#a855f7>Ожидание игроков</#a855f7>"),
    MINING("<green>Добыча ресурсов</green>"),
    SHRINKING("<yellow>Сужение зоны</yellow>"),
    FINAL("<red><bold>Финальная битва</bold></red>"),
    ENDING("<#c084fc>Завершение</#c084fc>");

    private final String formatted;

    MatchPhase(String formatted) {
        this.formatted = formatted;
    }

    public String getFormatted() {
        return formatted;
    }
}
