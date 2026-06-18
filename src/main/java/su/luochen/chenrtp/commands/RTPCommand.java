package su.luochen.chenrtp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.luochen.chenrtp.ChenRTP;
import su.luochen.chenrtp.utils.MessageUtil;

/**
 * RTP 命令处理器
 * /rtp     - 随机传送
 * /rtpreload - 重载配置
 */
public class RTPCommand implements CommandExecutor {

    private final ChenRTP plugin;

    public RTPCommand(ChenRTP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName().toLowerCase();

        switch (cmdName) {
            case "rtp":
                return handleRTP(sender, args);
            case "rtpreload":
                return handleReload(sender);
            default:
                return false;
        }
    }

    /**
     * 处理 /rtp 命令
     */
    private boolean handleRTP(CommandSender sender, String[] args) {
        // 只有玩家可以使用
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&c只有玩家可以使用此命令！");
            return true;
        }

        Player player = (Player) sender;

        // 权限检查
        if (!player.hasPermission("chenrtp.use")) {
            MessageUtil.sendPrefixedMessage(player, plugin.getRtpManager().getPrefix(),
                    "&c你没有权限使用此命令！");
            return true;
        }

        // 执行随机传送
        plugin.getRtpManager().randomTeleport(player);
        return true;
    }

    /**
     * 处理 /rtpreload 命令
     */
    private boolean handleReload(CommandSender sender) {
        // 权限检查
        if (!sender.hasPermission("chenrtp.admin")) {
            MessageUtil.sendPrefixedMessage(sender, plugin.getRtpManager().getPrefix(),
                    "&c你没有权限使用此命令！");
            return true;
        }

        // 重载配置
        plugin.reloadConfig();
        plugin.getRtpManager().loadConfig();
        MessageUtil.sendPrefixedMessage(sender, plugin.getRtpManager().getPrefix(),
                "&a配置已重新加载！");
        return true;
    }
}
