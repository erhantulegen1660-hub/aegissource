package com.aegis.resourcepack;

import com.aegis.AegisPlugin;
import com.aegis.legendary.LegendaryItem;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LegendaryPackBuilder {

    private final AegisPlugin plugin;

    public LegendaryPackBuilder(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void build(File targetZip) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetZip))) {
            writeEntry(zos, "pack.mcmeta", """
                {
                  "pack": {
                    "pack_format": 15,
                    "description": "AEGIS legendary items"
                  }
                }
                """);

            StringBuilder swordOverrides = new StringBuilder();
            StringBuilder axeOverrides = new StringBuilder();

            for (LegendaryItem item : plugin.getLegendaryRecipeRegistry().getAll()) {
                String id = item.getId();
                int cmd = item.getCustomModelData();
                boolean isAxe = item.getMaterial().name().endsWith("_AXE");

                writeEntry(zos, "assets/minecraft/models/item/" + id + ".json", """
                    {
                      "parent": "minecraft:item/handheld",
                      "textures": {
                        "layer0": "minecraft:item/%s"
                      }
                    }
                    """.formatted(id));

                writeTexture(zos, "assets/minecraft/textures/item/" + id + ".png", cmd);

                String overrideLine = """
                    { "predicate": { "custom_model_data": %d }, "model": "minecraft:item/%s" },""".formatted(cmd, id);
                if (isAxe) axeOverrides.append(overrideLine);
                else swordOverrides.append(overrideLine);
            }

            writeBaseOverride(zos, "netherite_sword", swordOverrides.toString());
            writeBaseOverride(zos, "netherite_axe", axeOverrides.toString());
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сборки legendary.zip: " + e.getMessage());
        }
    }

    private void writeBaseOverride(ZipOutputStream zos, String baseItem, String overrides) throws IOException {
        String clean = overrides.endsWith(",") ? overrides.substring(0, overrides.length() - 1) : overrides;
        writeEntry(zos, "assets/minecraft/models/item/" + baseItem + ".json", """
            {
              "parent": "minecraft:item/handheld",
              "textures": {
                "layer0": "minecraft:item/%s"
              },
              "overrides": [
                %s
              ]
            }
            """.formatted(baseItem, clean));
    }

    private void writeEntry(ZipOutputStream zos, String path, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(path));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private void writeTexture(ZipOutputStream zos, String path, int seed) throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(168, 85, 247));
        for (int i = 2; i < 14; i++) {
            g.fillRect(i, 15 - i, 2, 2);
        }
        g.setColor(new Color(192, 132, 252));
        g.fillRect(7, 7, 3, 3);
        g.dispose();

        zos.putNextEntry(new ZipEntry(path));
        ImageIO.write(img, "PNG", zos);
        zos.closeEntry();
    }
}
