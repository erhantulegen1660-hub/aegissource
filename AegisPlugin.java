package com.aegis;

import com.aegis.casepkg.CaseManager;
import com.aegis.chat.ChatListener;
import com.aegis.chat.ChatManager;
import com.aegis.chat.PrivateMessageService;
import com.aegis.chat.VipChatService;
import com.aegis.classes.ClassService;
import com.aegis.config.ConfigManager;
import com.aegis.cosmetic.CosmeticManager;
import com.aegis.data.DatabaseManager;
import com.aegis.donate.DonateCommand;
import com.aegis.donate.DonateMenuListener;
import com.aegis.donate.DonateService;
import com.aegis.hub.*;
import com.aegis.legendary.*;
import com.aegis.match.*;
import com.aegis.resourcepack.ResourcePackGenerator;
import com.aegis.status.OpGuard;
import com.aegis.status.StatusManager;
import com.aegis.status.TitleService;
import com.aegis.status.VipColorService;
import com.aegis.team.TeamListener;
import com.aegis.team.TeamManager;
import com.aegis.top.TopManager;
import com.aegis.util.Mini;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class AegisPlugin extends JavaPlugin {

    private static AegisPlugin instance;

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private StatusManager statusManager;
    private VipColorService vipColorService;
    private TitleService titleService;
    private OpGuard opGuard;
    private ChatManager chatManager;
    private PrivateMessageService privateMessageService;
    private VipChatService vipChatService;
    private HubService hubService;
    private FlyService flyService;
    private SizeService sizeService;
    private NpcVillagerService npcVillagerService;
    private TeamManager teamManager;
    private ClassService classService;
    private LegendaryRecipeRegistry legendaryRecipeRegistry;
    private LegendaryManager legendaryManager;
    private CosmeticManager cosmeticManager;
    private CaseManager caseManager;
    private TopManager topManager;
    private DonateService donateService;
    private MatchManager matchManager;
    private ResourcePackGenerator resourcePackGenerator;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.init();

        this.opGuard = new OpGuard(this);
        this.vipColorService = new VipColorService();
        this.titleService = new TitleService(this);
        this.statusManager = new StatusManager(this, vipColorService, titleService, opGuard);

        this.chatManager = new ChatManager(this);
        this.privateMessageService = new PrivateMessageService(this);
        this.vipChatService = new VipChatService(this);

        this.cosmeticManager = new CosmeticManager(this);
        this.legendaryRecipeRegistry = new LegendaryRecipeRegistry(this);
        this.legendaryRecipeRegistry.loadRecipes();
        this.legendaryManager = new LegendaryManager(this, legendaryRecipeRegistry);

        this.classService = new ClassService(this);
        this.teamManager = new TeamManager(this);
        this.donateService = new DonateService(this);
        this.topManager = new TopManager(this);
        this.caseManager = new CaseManager(this);

        this.hubService = new HubService(this);
        this.flyService = new FlyService(this);
        this.sizeService = new SizeService(this);
        this.npcVillagerService = new NpcVillagerService(this);

        this.matchManager = new MatchManager(this);
        this.matchManager.initLobbies();

        this.resourcePackGenerator = new ResourcePackGenerator(this);
        this.resourcePackGenerator.generatePacks();

        registerListeners();
        registerPlaceholderApi();

        getLogger().info("AEGIS v" + getPluginMeta().getVersion() + " успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (matchManager != null) matchManager.shutdown();
        if (topManager != null) topManager.shutdown();
        if (databaseManager != null) databaseManager.close();
        getLogger().info("AEGIS отключён.");
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ChatListener(this, chatManager), this);
        pm.registerEvents(new HubListener(this, hubService), this);
        pm.registerEvents(new TeamListener(this, teamManager), this);
        pm.registerEvents(new MatchListener(this, matchManager), this);
        pm.registerEvents(new SmelterPickaxeListener(this, matchManager), this);
        pm.registerEvents(new MatchCraftLimitListener(this, matchManager), this);
        pm.registerEvents(new RecipeBookListener(this, legendaryManager), this);
        pm.registerEvents(new LegendaryAbilityListener(this, legendaryManager, matchManager), this);
        pm.registerEvents(new DonateMenuListener(this, donateService), this);
        pm.registerEvents(caseManager, this);
        pm.registerEvents(npcVillagerService, this);
    }

    private void registerPlaceholderApi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AegisPlaceholderExpansion(this).register();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String cmd = command.getName().toLowerCase();
        switch (cmd) {
            case "hub" -> { if (sender instanceof Player p) hubService.sendToHub(p); return true; }
            case "tell" -> { privateMessageService.handleTell(sender, args); return true; }
            case "reply" -> { privateMessageService.handleReply(sender, args); return true; }
            case "vipchat" -> { vipChatService.handleVipChat(sender, args); return true; }
            case "check" -> { chatManager.handleCheckCommand(sender, args); return true; }
            case "donate" -> { new DonateCommand(this).execute(sender, args); return true; }
            case "vip" -> { if (sender instanceof Player p) statusManager.handleVipCommand(p, args); return true; }
            case "suffix" -> { if (sender instanceof Player p) titleService.handleSuffixCommand(p, args); return true; }
            case "team" -> { if (sender instanceof Player p) teamManager.handleCommand(p, args); return true; }
            case "class" -> { if (sender instanceof Player p) classService.openClassMenu(p); return true; }
            case "case" -> { if (sender instanceof Player p) caseManager.openCaseMenu(p); return true; }
            case "top" -> { topManager.handleCommand(sender, args); return true; }
            case "legend" -> { if (sender instanceof Player p) new RecipeBookGui(this).openList(p, 0); return true; }
            case "fly" -> { if (sender instanceof Player p) flyService.toggleFly(p); return true; }
            case "size" -> { if (sender instanceof Player p) sizeService.handleSizeCommand(p, args); return true; }
            case "aegis" -> { handleAdminCommand(sender, args); return true; }
        }
        return false;
    }

    private void handleAdminCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("aegis.admin")) {
            sender.sendMessage(Mini.parse(configManager.getMessage("no-permission")));
            return;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(Mini.parse("<#a855f7>/aegis reload</#a855f7> <gray>- перезагрузка конфигов</gray>"));
            sender.sendMessage(Mini.parse("<#a855f7>/aegis setrole <ник> <роль> [дни]</#a855f7> <gray>- выдать роль</gray>"));
            sender.sendMessage(Mini.parse("<#a855f7>/aegis amethyst <give|take|set> <ник> <сумма></#a855f7> <gray>- баланс</gray>"));
            sender.sendMessage(Mini.parse("<#a855f7>/aegis token <ник></#a855f7> <gray>- выдать токен титула</gray>"));
            sender.sendMessage(Mini.parse("<#a855f7>/aegis pack</#a855f7> <gray>- пересобрать ZIP ресурспаков</gray>"));
            return;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                configManager.loadAll();
                legendaryRecipeRegistry.loadRecipes();
                sender.sendMessage(Mini.parse(configManager.getMessage("reload-success")));
            }
            case "pack" -> {
                resourcePackGenerator.generatePacks();
                sender.sendMessage(Mini.parse("<#c084fc>Ресурспаки legendary.zip и cosmetic.zip пересобраны!</#c084fc>"));
            }
            case "setrole" -> {
                if (args.length < 3) return;
                int days = args.length >= 4 ? Integer.parseInt(args[3]) : -1;
                statusManager.setRoleByName(args[1], args[2], days);
                sender.sendMessage(Mini.parse("<#c084fc>Роль <role> установлена игроку <player>.</#c084fc>",
                        Placeholder.unparsed("role", args[2]), Placeholder.unparsed("player", args[1])));
            }
            case "amethyst" -> {
                if (args.length < 4) return;
                donateService.processAmethystCommand(args[1], args[2], Integer.parseInt(args[3]));
                sender.sendMessage(Mini.parse("<#c084fc>Операция с аметистами выполнена.</#c084fc>"));
            }
            case "token" -> {
                if (args.length < 2) return;
                titleService.grantTitleTokenByName(args[1]);
                sender.sendMessage(Mini.parse("<#c084fc>Токен титула выдан игроку " + args[1] + ".</#c084fc>"));
            }
        }
    }

    public static AegisPlugin getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public StatusManager getStatusManager() { return statusManager; }
    public VipColorService getVipColorService() { return vipColorService; }
    public TitleService getTitleService() { return titleService; }
    public ChatManager getChatManager() { return chatManager; }
    public HubService getHubService() { return hubService; }
    public MatchManager getMatchManager() { return matchManager; }
    public TeamManager getTeamManager() { return teamManager; }
    public ClassService getClassService() { return classService; }
    public LegendaryManager getLegendaryManager() { return legendaryManager; }
    public LegendaryRecipeRegistry getLegendaryRecipeRegistry() { return legendaryRecipeRegistry; }
    public CaseManager getCaseManager() { return caseManager; }
    public CosmeticManager getCosmeticManager() { return cosmeticManager; }
    public TopManager getTopManager() { return topManager; }
    public DonateService getDonateService() { return donateService; }
}
