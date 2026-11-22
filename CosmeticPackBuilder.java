package com.aegis.resourcepack;

import com.aegis.AegisPlugin;
import com.aegis.cosmetic.CosmeticItem;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CosmeticPackBuilder {

    private final AegisPlugin plugin;

    public CosmeticPackBuilder(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void build(File targetZip) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetZip))) {
            writeEntry(zos, "pack.mcmeta", """
                {
                  "pack": {
                    "pack_format": 15,
                    "description": "AEGIS cosmetics"
                  }
                }
                """);

            StringBuilder overrides = new StringBuilder();
            for (CosmeticItem item : plugin.getCosmeticManager().getAllCosmetics()) {
                String id = item.id();
                int cmd = item.customModelData();

                writeEntry(zos, "assets/minecraft/models/item/" + id + ".json", """
                    {
                      "parent": "minecraft:item/generated",
                      "textures": {
                        "layer0": "minecraft:item/%s"
                      },
                      "display": {
                        "head": {
                          "rotation": [0, 0, 0],
                          "translation": [0, 6.5, 0],
                          "scale": [1.05, 1.05, 1.05]
                        }
                      }
                    }
                    """.formatted(id));

                writeTexture(zos, "assets/minecraft/textures/item/" + id + ".png", cmd);
                overrides.append("""
                    { "predicate": { "custom_model_data": %d }, "model": "minecraft:item/%s" },""".formatted(cmd, id));
            }

            String clean = overrides.toString();
            if (clean.endsWith(",")) clean = clean.substring(0, clean.length() - 1);

            writeEntry(zos, "assets/minecraft/models/item/carved_pumpkin.json", """
                {
                  "parent": "minecraft:block/carved_pumpkin",
                  "overrides": [
                    %s
                  ]
                }
                """.formatted(clean));
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сборки cosmetic.zip: " + e.getMessage());
        }
    }

    private void writeEntry(ZipOutputStream zos, String path, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(path));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private void writeTexture(ZipOutputStream zos, String path, int seed) throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(124, 58, 237));
        g.fillRect(3, 4, 10, 7);
        g.setColor(new Color(232, 121, 249));
        g.fillRect(2, 10, 12, 2);
        g.dispose();

        zos.putNextEntry(new ZipEntry(path));
        ImageIO.write(img, "PNG", zos);
        zos.closeEntry();
    }
}
