package com.aegis.donate;

import com.aegis.AegisPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DonateCommand {

    private final AegisPlugin plugin;

    public DonateCommand(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player p) {
            plugin.getDonateService().openMainMenu(p);
        }
    }
}
