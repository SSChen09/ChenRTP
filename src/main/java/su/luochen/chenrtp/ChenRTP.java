package su.luochen.chenrtp;

import org.bukkit.plugin.java.JavaPlugin;
import su.luochen.chenrtp.commands.RTPCommand;
import su.luochen.chenrtp.manager.RTPManager;

public final class ChenRTP extends JavaPlugin {

    private static ChenRTP instance;
    private RTPManager rtpManager;

    @Override
    public void onEnable() {
        instance = this;

        // 保存默认配置
        saveDefaultConfig();

        // 初始化管理器
        rtpManager = new RTPManager(this);

        // 注册命令
        getCommand("rtp").setExecutor(new RTPCommand(this));
        getCommand("rtpreload").setExecutor(new RTPCommand(this));

        getLogger().info("§aChenRTP 插件已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("§cChenRTP 插件已禁用！");
    }

    public static ChenRTP getInstance() {
        return instance;
    }

    public RTPManager getRtpManager() {
        return rtpManager;
    }
}
