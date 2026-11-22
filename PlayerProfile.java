package com.aegis.data;

import com.aegis.classes.GameClass;
import java.util.*;

public class PlayerProfile {

    private final UUID uuid;
    private String username;
    private String role;
    private long roleExpireAt;
    private String vipPlusColor;
    private boolean hasTitleToken;
    private String customSuffix;
    private int amethysts;
    private int kills;
    private int deaths;
    private int wins;
    private int losses;
    private int craftedLegendaries;
    private long playTimeSeconds;
    private boolean freeCaseClaimed;
    private String activeCosmeticId;
    private final Set<String> unlockedCosmetics = new HashSet<>();
    private GameClass selectedClass;
    private final Map<GameClass, Integer> classLevels = new EnumMap<>(GameClass.class);

    public PlayerProfile(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.role = "default";
        this.roleExpireAt = 0L;
        this.vipPlusColor = "purple";
        this.hasTitleToken = false;
        this.customSuffix = "";
        this.amethysts = 0;
        this.activeCosmeticId = "classic_hat";
        this.unlockedCosmetics.add("classic_hat");
        this.selectedClass = GameClass.MINER;
        for (GameClass gc : GameClass.values()) {
            classLevels.put(gc, 1);
        }
    }

    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public long getRoleExpireAt() { return roleExpireAt; }
    public void setRoleExpireAt(long roleExpireAt) { this.roleExpireAt = roleExpireAt; }
    public String getVipPlusColor() { return vipPlusColor; }
    public void setVipPlusColor(String vipPlusColor) { this.vipPlusColor = vipPlusColor; }
    public boolean isHasTitleToken() { return hasTitleToken; }
    public void setHasTitleToken(boolean hasTitleToken) { this.hasTitleToken = hasTitleToken; }
    public String getCustomSuffix() { return customSuffix; }
    public void setCustomSuffix(String customSuffix) { this.customSuffix = customSuffix; }
    public int getAmethysts() { return amethysts; }
    public void setAmethysts(int amethysts) { this.amethysts = Math.max(0, amethysts); }
    public void addAmethysts(int amount) { this.amethysts = Math.max(0, this.amethysts + amount); }
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    public int getCraftedLegendaries() { return craftedLegendaries; }
    public void setCraftedLegendaries(int craftedLegendaries) { this.craftedLegendaries = craftedLegendaries; }
    public long getPlayTimeSeconds() { return playTimeSeconds; }
    public void setPlayTimeSeconds(long playTimeSeconds) { this.playTimeSeconds = playTimeSeconds; }
    public boolean isFreeCaseClaimed() { return freeCaseClaimed; }
    public void setFreeCaseClaimed(boolean freeCaseClaimed) { this.freeCaseClaimed = freeCaseClaimed; }
    public String getActiveCosmeticId() { return activeCosmeticId; }
    public void setActiveCosmeticId(String activeCosmeticId) { this.activeCosmeticId = activeCosmeticId; }
    public Set<String> getUnlockedCosmetics() { return unlockedCosmetics; }
    public GameClass getSelectedClass() { return selectedClass; }
    public void setSelectedClass(GameClass selectedClass) { this.selectedClass = selectedClass; }
    public Map<GameClass, Integer> getClassLevels() { return classLevels; }
    public int getClassLevel(GameClass gc) { return classLevels.getOrDefault(gc, 1); }
}
