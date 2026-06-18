package su.luochen.chenrtp.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * 消息工具类，用于格式化和发送消息
 */
public class MessageUtil {

    /**
     * 将颜色代码（如 &a）转换为 Minecraft 的 §a 格式
     */
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * 向 CommandSender 发送带颜色的消息
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    /**
     * 向 CommandSender 发送前缀消息
     */
    public static void sendPrefixedMessage(CommandSender sender, String prefix, String message) {
        sender.sendMessage(colorize(prefix + message));
    }
}
