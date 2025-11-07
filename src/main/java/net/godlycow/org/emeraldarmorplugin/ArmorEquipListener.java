package net.godlycow.org.emeraldarmorplugin;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

public class ArmorEquipListener implements Listener {

    private final EmeraldArmorPlugin plugin;
    private final ConcurrentHashMap<UUID, BukkitRunnable> playing = new ConcurrentHashMap<>();

    private final Map<UUID, Long> equipCooldown = new HashMap<>();
    private final long COOLDOWN_MS = 15000;

    public ArmorEquipListener(EmeraldArmorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkAndApply(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playing.remove(event.getPlayer().getUniqueId());
        equipCooldown.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Bukkit.getScheduler().runTask(plugin, () -> checkAndApply(player));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && isEmeraldArmorPiece(item)) {
            UUID id = player.getUniqueId();
            long now = System.currentTimeMillis();

            if (equipCooldown.containsKey(id) && now - equipCooldown.get(id) < COOLDOWN_MS) {
                player.sendMessage("§cYou must wait before equipping another Emerald Armor piece!");
                return;
            }

            equipCooldown.put(id, now);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (isWearingEmerald(player)) {
                    playEquipAnimation(player);
                }
                checkAndApply(player);
            });
        }
    }

    private boolean isWearingEmerald(Player p) {
        for (ItemStack it : p.getInventory().getArmorContents()) {
            if (isEmeraldArmorPiece(it)) return true;
        }
        return false;
    }

    private void checkAndApply(Player p) {

    }

    private boolean isEmeraldArmorPiece(ItemStack item) {
        if (item == null) return false;
        if (!item.getType().name().startsWith("LEATHER_")) return false;
        if (!(item.getItemMeta() instanceof LeatherArmorMeta meta)) return false;
        Color c = meta.getColor();
        return c != null && c.equals(Color.GREEN);
    }

    private void playEquipAnimation(Player p) {
        if (!plugin.getCfg().getBoolean("animation.enabled", true)) return;

        UUID id = p.getUniqueId();
        if (playing.containsKey(id)) return;

        BukkitRunnable task = new BukkitRunnable() {
            int ticks = plugin.getCfg().getInt("animation.particle-duration-ticks", 40);
            int interval = plugin.getCfg().getInt("animation.particle-interval-ticks", 2);
            int i = 0;

            @Override
            public void run() {
                if (i > ticks) {
                    cancel();
                    playing.remove(id);
                    return;
                }

                p.getWorld().spawnParticle(
                        Particle.valueOf(plugin.getCfg().getString("animation.particle-type", "VILLAGER_HAPPY")),
                        p.getLocation().add(0, 1, 0),
                        20,
                        0.5,
                        0.5,
                        0.5
                );

                p.playSound(p.getLocation(), "entity.player.levelup", 1f, 1f);
                i += interval;
            }
        };
        task.runTaskTimer(plugin, 0, plugin.getCfg().getInt("animation.particle-interval-ticks", 2));
        playing.put(id, task);
    }
}
