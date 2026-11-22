package com.aegis.resourcepack;

import com.aegis.AegisPlugin;
import java.io.File;

public class ResourcePackGenerator {

    private final AegisPlugin plugin;

    public ResourcePackGenerator(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void generatePacks() {
        File outDir = new File(plugin.getDataFolder(), "packs");
        if (!outDir.exists()) outDir.mkdirs();

        new LegendaryPackBuilder(plugin).build(new File(outDir, "legendary.zip"));
        new CosmeticPackBuilder(plugin).build(new File(outDir, "cosmetic.zip"));
    }
}
