package com.aegis.legendary;

import com.aegis.AegisPlugin;
import com.aegis.match.MatchManager;
import com.aegis.util.Mini;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class LegendaryAbilityListener implements Listener {

    private final AegisPlugin plugin;
    private final LegendaryManager legendaryManager;
    private final MatchManager matchManager;

    public LegendaryAbilityListener(AegisPlugin plugin, LegendaryManager legendaryManager, MatchManager matchManager) {
        this.plugin = plugin;
        this.legendaryManager = legendaryManager;
        this.matchManager = matchManager;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = event.getPlayer();
        LegendaryItem leg = legendaryManager.identify(event.getItem());
        if (leg == null) return;

        long cd = legendaryManager.getRemainingCooldown(p.getUniqueId(), leg.getId());
        if (cd > 0) {
            p.sendActionBar(Mini.parse("<red>Перезарядка способности: " + cd + " сек.</red>"));
            return;
        }

        legendaryManager.setCooldown(p.getUniqueId(), leg.getId(), leg.getCooldownSeconds());
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.2f, 1.2f);
        p.getWorld().spawnParticle(Particle.WITCH, p.getLocation().add(0, 1, 0), 25, 0.6, 0.6, 0.6, 0.05);

        switch (leg.getId()) {
            case "aegis_blade" -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 120, 1));
                for (Entity e : p.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity le && !e.equals(p)) {
                        Vector knock = e.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.4).setY(0.5);
                        le.setVelocity(knock);
                    }
                }
            }
            case "void_reaper" -> {
                Vector dash = p.getLocation().getDirection().normalize().multiply(2.1).setY(0.35);
                p.setVelocity(dash);
                for (Entity e : p.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity le && !e.equals(p)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                    }
                }
            }
            case "thunder_cleaver" -> {
                Location target = p.getTargetBlockExact(20) != null ? p.getTargetBlockExact(20).getLocation() : p.getLocation().add(p.getLocation().getDirection().multiply(10));
                p.getWorld().strikeLightning(target);
            }
            case "vampire_fang" -> {
                double healed = 0;
                for (Entity e : p.getNearbyEntities(4, 4, 4)) {
                    if (e instanceof LivingEntity le && !e.equals(p)) {
                        le.damage(4.0, p);
                        healed += 2.0;
                    }
                }
                p.setHealth(Math.min(20.0, p.getHealth() + healed));
            }
            case "frost_mourne" -> {
                for (Entity e : p.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity le && !e.equals(p)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 4));
                        le.setFreezeTicks(140);
                    }
                }
            }
            case "phoenix_greatsword" -> {
                for (Entity e : p.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity le && !e.equals(p)) {
                        le.setFireTicks(100);
                        le.setVelocity(new Vector(0, 0.85, 0));
                    }
                }
            }
            case "earthshaker_axe" -> {
                for (Entity e : p.getNearbyEntities(6, 4, 6)) {
                    if (e instanceof LivingEntity le && !e.equals(p)) {
                        le.damage(6.0, p);
                        le.setVelocity(new Vector(0, 0.7, 0));
                    }
                }
            }
            case "shadow_dancer" -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 80, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1));
            }
            case "starlight_sabre" -> {
                for (Entity e : p.getNearbyEntities(7, 5, 7)) {
                    if (e instanceof LivingEntity le && !e.equals(p)) {
                        le.damage(6.0, p);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
                        break;
                    }
                }
            }
            case "crown_breaker" -> {
                p.setVelocity(p.getLocation().getDirection().normalize().multiply(1.6).setY(0.25));
                for (Entity e : p.getNearbyEntities(4, 4, 4)) {
                    if (e instanceof LivingEntity le && !e.equals(p)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        LegendaryItem leg = legendaryManager.identify(attacker.getInventory().getItemInMainHand());
        if (leg == null) return;

        switch (leg.getId()) {
            case "aegis_blade" -> attacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0));
            case "void_reaper" -> victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
            case "thunder_cleaver" -> {
                if (ThreadLocalRandom.current().nextInt(100) < 20) {
                    victim.getWorld().strikeLightningEffect(victim.getLocation());
                    event.setDamage(event.getDamage() + 3.0);
                }
            }
            case "vampire_fang" -> attacker.setHealth(Math.min(20.0, attacker.getHealth() + 1.5));
            case "frost_mourne" -> victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
            case "phoenix_greatsword" -> {
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0));
                victim.setFireTicks(80);
            }
            case "starlight_sabre" -> victim.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
        }
    }
}
