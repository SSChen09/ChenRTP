package su.luochen.chenrtp.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import su.luochen.chenrtp.ChenRTP;
import su.luochen.chenrtp.utils.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * RTP 管理器，负责随机传送的核心逻辑
 */
public class RTPManager {

    private final ChenRTP plugin;
    private final Random random;
    private final Map<String, Long> cooldowns;

    // 配置项
    private int radius;
    private int maxAttempts;
    private int cooldownTime;
    private String prefix;
    private boolean avoidWater;
    private boolean avoidLava;
    private boolean avoidMobs;

    public RTPManager(ChenRTP plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.cooldowns = new HashMap<>();
        loadConfig();
    }

    /**
     * 从配置文件加载设置
     */
    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        radius = config.getInt("radius", 10000);
        maxAttempts = config.getInt("max-attempts", 100);
        cooldownTime = config.getInt("cooldown", 30);
        prefix = config.getString("prefix", "&6[&eRTP&6] &7");
        avoidWater = config.getBoolean("avoid-water", true);
        avoidLava = config.getBoolean("avoid-lava", true);
        avoidMobs = config.getBoolean("avoid-mobs", true);
    }

    /**
     * 对玩家执行随机传送
     */
    public void randomTeleport(Player player) {
        String playerName = player.getName();

        // 检查冷却
        if (cooldowns.containsKey(playerName)) {
            long lastUse = cooldowns.get(playerName);
            long elapsed = (System.currentTimeMillis() - lastUse) / 1000;
            if (elapsed < cooldownTime) {
                long remaining = cooldownTime - elapsed;
                MessageUtil.sendPrefixedMessage(player, prefix,
                        configMessage("on-cooldown", "&c你还需要等待 &e" + remaining + " &c秒才能再次使用！"));
                return;
            }
        }

        World world = player.getWorld();
        Location playerLocation = player.getLocation().clone();
        MessageUtil.sendPrefixedMessage(player, prefix,
                configMessage("teleporting", "&a正在为你寻找安全的随机位置..."));

        // 异步查找安全位置，避免卡服
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Location safeLocation = findSafeLocation(playerLocation);

            if (safeLocation == null) {
                // 找不到安全位置，在玩家当前位置附近随机传送
                safeLocation = playerLocation.clone();
                safeLocation.add(random.nextInt(200) - 100, 0, random.nextInt(200) - 100);
                int y = world.getHighestBlockYAt(safeLocation);
                safeLocation.setY(y + 1);
                MessageUtil.sendPrefixedMessage(player, prefix,
                        configMessage("fallback-teleport", "&e未能找到理想位置，已传送到当前位置附近。"));
            }

            // 回到主线程执行传送
            final Location finalLocation = safeLocation;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.teleport(finalLocation);
                cooldowns.put(playerName, System.currentTimeMillis());
                MessageUtil.sendPrefixedMessage(player, prefix,
                        configMessage("teleport-success", "&a随机传送成功！"));
            });
        });
    }

    /**
     * 以玩家当前位置为中心，查找安全的传送位置
     */
    private Location findSafeLocation(Location center) {
        World world = center.getWorld();
        if (world == null) return null;

        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();

        for (int i = 0; i < maxAttempts; i++) {
            int x = centerX + random.nextInt(radius * 2) - radius;
            int z = centerZ + random.nextInt(radius * 2) - radius;

            // 获取该位置最高的非空气方块
            int y = world.getHighestBlockYAt(x, z);
            Location location = new Location(world, x + 0.5, y + 1, z + 0.5);

            if (isSafeLocation(location)) {
                return location;
            }
        }
        return null;
    }

    /**
     * 检查位置是否安全
     */
    private boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) return false;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        // 检查玩家脚下是否有方块
        Material feetBlock = world.getBlockAt(x, y - 1, z).getType();
        if (feetBlock == Material.AIR || feetBlock == Material.CAVE_AIR || feetBlock == Material.VOID_AIR) {
            return false;
        }

        // 检查玩家所在位置是否有空间
        Material feet = world.getBlockAt(x, y, z).getType();
        if (feet != Material.AIR && feet != Material.CAVE_AIR) {
            return false;
        }

        // 检查头部位置
        Material head = world.getBlockAt(x, y + 1, z).getType();
        if (head != Material.AIR && head != Material.CAVE_AIR) {
            return false;
        }

        // 避开水
        if (avoidWater && (feetBlock == Material.WATER || feetBlock == Material.LAVA)) {
            return false;
        }

        // 避免岩浆
        if (avoidLava && feetBlock == Material.LAVA) {
            return false;
        }

        // 避免危险方块
        Material[] dangerousBlocks = {
                Material.CACTUS, Material.MAGMA_BLOCK, Material.FIRE, Material.SOUL_FIRE,
                Material.COBWEB, Material.POINTED_DRIPSTONE
        };
        for (Material dangerous : dangerousBlocks) {
            if (feetBlock == dangerous || feet == dangerous) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取配置消息，支持自定义
     */
    private String configMessage(String key, String defaultValue) {
        return plugin.getConfig().getString("messages." + key, defaultValue);
    }

    /**
     * 检查并设置冷却
     */
    public boolean hasCooldown(String playerName) {
        if (!cooldowns.containsKey(playerName)) return false;
        long elapsed = (System.currentTimeMillis() - cooldowns.get(playerName)) / 1000;
        return elapsed < cooldownTime;
    }

    // Getters
    public int getRadius() { return radius; }
    public int getCooldownTime() { return cooldownTime; }
    public String getPrefix() { return prefix; }
}
