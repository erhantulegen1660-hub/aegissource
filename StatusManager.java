package com.aegis.status;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class StatusManager {

    private final AegisPlugin plugin;
    private final VipColorService vipColorService;
    private final TitleService titleService;
    private final OpGuard opGuard;

    public StatusManager(AegisPlugin plugin, VipColorService vipColorService, TitleService titleService, OpGuard opGuard) {
        this.plugin = plugin;
        this.vipColorService = vipColorService;
        this.titleService = titleService;
        this.opGuard = opGuard;
    }

    public String getEffectiveRole(PlayerProfile profile) {
        if (profile.getRoleExpireAt() > 0 && System.currentTimeMillis() > profile.getRoleExpireAt()) {
            profile.setRole("default");
            profile.setRoleExpireAt(0L);
            plugin.getDatabaseManager().saveProfile(profile);
        }
        return profile.getRole().toLowerCase();
    }

    public Component formatBadge(PlayerProfile profile) {
        String role = getEffectiveRole(profile);
        return switch (role) {
            case "owner" -> Mini.parse("<#d946ef>[<white><bold>OWNER</bold></white>]</#d946ef>");
            case "admin" -> Mini.parse("<#dc2626>[<white><bold>ADMIN</bold></white>]</#dc2626>");
            case "builder" -> Mini.parse("<#a855f7>[<white><bold>BUILDER</bold></white>]</#a855f7>");
            case "media" -> Mini.parse("<#dc2626>[<white><bold>MEDIA</bold></white>]</#dc2626>");
            case "stmod" -> Mini.parse("<#16a34a>[<white><bold>ST.MOD</bold></white>]</#16a34a>");
            case "mod" -> Mini.parse("<#16a34a>[<white><bold>MOD</bold></white>]</#16a34a>");
            case "hmod" -> Mini.parse("<#16a34a>[<white><bold>H.MOD</bold></white>]</#16a34a>");
            case "sthelper" -> Mini.parse("<#0ea5e9>[<white><bold>ST.HELPER</bold></white>]</#0ea5e9>");
            case "helper" -> Mini.parse("<#0ea5e9>[<white><bold>HELPER</bold></white>]</#0ea5e9>");
            case "vip+" -> {
                VipColorService.BadgeStyle style = vipColorService.getStyle(profile.getVipPlusColor());
                yield Mini.parse("<" + style.bgHex() + ">[<" + style.textHex() + "><bold>VIP+</bold></" + style.textHex() + ">]</" + style.bgHex() + ">");
            }
            case "vip" -> Mini.parse("<#a855f7>[<white><bold>VIP</bold></white>]</#a855f7>");
            default -> Mini.parse("<#ffffff>[<black>ИГРОК</black>]</#ffffff>");
        };
    }

    public void setRoleByName(String playerName, String role, int days) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        PlayerProfile profile = plugin.getDatabaseManager().getProfile(target.getUniqueId());
        profile.setRole(role.toLowerCase());
        if (days > 0) {
            profile.setRoleExpireAt(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days));
        } else {
            profile.setRoleExpireAt(0L);
        }
        plugin.getDatabaseManager().saveProfile(profile);
        if (target.isOnline() && target.getPlayer() != null) {
            opGuard.syncOp(target.getPlayer(), profile.getRole());
        }
    }

    public void handleVipCommand(Player player, String[] args) {
        PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
        if (!"vip+".equalsIgnoreCase(getEffectiveRole(profile)) && !player.hasPermission("aegis.admin")) {
            player.sendMessage(Mini.parse("<red>Выбор цвета плашки доступен только обладателям статуса VIP+.</red>"));
            return;
        }
        if (args.length < 2 || !args[0].equalsIgnoreCase("color")) {
            player.sendMessage(Mini.parse("<#a855f7>Использование: /vip color <цвет></#a855f7>"));
            return;
        }
        String colorKey = args[1].toLowerCase();
        if (!vipColorService.exists(colorKey)) {
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("status.vip-color-invalid")));
            return;
        }
        profile.setVipPlusColor(colorKey);
        plugin.getDatabaseManager().saveProfile(profile);
        String rus = vipColorService.getStyle(colorKey).russianName();
        player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("status.vip-color-changed"),
                Placeholder.unparsed("color_name", rus)));
    }

    public boolean isStaffOrVipPlus(PlayerProfile profile) {
        String r = getEffectiveRole(profile);
        return r.equals("vip+") || r.equals("owner") || r.equals("admin") || r.equals("builder")
                || r.equals("media") || r.contains("mod") || r.contains("helper");
    }

    public boolean isVipOrHigher(PlayerProfile profile) {
        return !getEffectiveRole(profile).equals("default");
    }

    public OpGuard getOpGuard() { return opGuard; }
}
