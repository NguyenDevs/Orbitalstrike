package com.NguyenDevs.orbitalstrike;

import com.NguyenDevs.orbitalstrike.managers.CannonManager;
import com.NguyenDevs.orbitalstrike.managers.CannonRecipeManager;
import com.NguyenDevs.orbitalstrike.managers.PayloadManager;
import com.NguyenDevs.orbitalstrike.commands.CannonCommand;
import com.NguyenDevs.orbitalstrike.managers.ConfigManager;
import com.NguyenDevs.orbitalstrike.managers.MessageManager;
import com.NguyenDevs.orbitalstrike.listeners.CannonInteractListener;
import com.NguyenDevs.orbitalstrike.listeners.CraftListener;
import com.NguyenDevs.orbitalstrike.listeners.TNTListener;
import com.NguyenDevs.orbitalstrike.utils.ConfigMigrationUtils;
import com.NguyenDevs.orbitalstrike.utils.SpigotPlugin;
import com.NguyenDevs.orbitalstrike.managers.WorldGuardManager;
import com.NguyenDevs.orbitalstrike.managers.TrailManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class OrbitalStrike extends JavaPlugin {

    private static OrbitalStrike instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private CannonManager cannonManager;
    private PayloadManager payloadManager;
    private CannonRecipeManager cannonRecipeManager;
    private WorldGuardManager worldGuardManager;
    private TrailManager trailManager;

    @Override
    public void onLoad() {
        WorldGuardManager.safeRegisterFlag();
    }


    @Override
    public void onEnable() {
        instance = this;

        this.worldGuardManager = new WorldGuardManager(this);
        this.worldGuardManager.init();

        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        ConfigMigrationUtils.migrateCannonsYml(this);
        
        this.messageManager = new MessageManager(this);
        this.messageManager.loadMessages();

        this.cannonManager = new CannonManager(this);
        this.trailManager = new TrailManager(this);
        this.trailManager.init();
        this.payloadManager = new PayloadManager(this);
        
        this.cannonRecipeManager = new CannonRecipeManager(this);
        this.cannonRecipeManager.registerRecipes();

        getCommand("cannon").setExecutor(new CannonCommand(this));

        getServer().getPluginManager().registerEvents(new CannonInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new TNTListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftListener(this), this);

        new SpigotPlugin(131685, this).checkForUpdate();

        printLogo();
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bOrbital&3Strike&9Cannon&8] &aOrbitalStrikeCannon plugin enabled successfully!"));
    }

    @Override
    public void onDisable() {
        if (cannonManager != null) {
            cannonManager.saveCannons();
        }
        if (payloadManager != null) {
            payloadManager.clearAll();
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bOrbital&3Strike&9Cannon&8] &cOrbitalStrikeCannon plugin disabled!"));
    }

    public static OrbitalStrike getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public CannonManager getCannonManager() {
        return cannonManager;
    }

    public PayloadManager getPayloadManager() {
        return payloadManager;
    }
    
    public CannonRecipeManager getCannonRecipeManager() {
        return cannonRecipeManager;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
    }

    public TrailManager getTrailManager() {
        return trailManager;
    }

    public void printLogo() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b    ██████╗ ██████╗ ██████╗ ██╗████████╗ █████╗ ██╗     "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ██╔═══██╗██╔══██╗██╔══██╗██║╚══██╔══╝██╔══██╗██║     "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ██║   ██║██████╔╝██████╔╝██║   ██║   ███████║██║     "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ██║   ██║██╔══██╗██╔══██╗██║   ██║   ██╔══██║██║     "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ╚██████╔╝██║  ██║██████╔╝██║   ██║   ██║  ██║███████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b    ╚═════╝ ╚═╝  ╚═╝╚═════╝ ╚═╝   ╚═╝   ╚═╝  ╚═╝╚══════╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3   ███████╗████████╗██████╗ ██╗██╗  ██╗███████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3   ██╔════╝╚══██╔══╝██╔══██╗██║██║ ██╔╝██╔════╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3   ███████╗   ██║   ██████╔╝██║█████╔╝ █████╗  "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3   ╚════██║   ██║   ██╔══██╗██║██╔═██╗ ██╔══╝  "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3   ███████║   ██║   ██║  ██║██║██║  ██╗███████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3   ╚══════╝   ╚═╝   ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝╚══════╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&9    ██████╗ █████╗ ███╗   ██╗███╗   ██╗ ██████╗ ███╗   ██╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&9   ██╔════╝██╔══██╗████╗  ██║████╗  ██║██╔═══██╗████╗  ██║"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&9   ██║     ███████║██╔██╗ ██║██╔██╗ ██║██║   ██║██╔██╗ ██║"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&9   ██║     ██╔══██║██║╚██╗██║██║╚██╗██║██║   ██║██║╚██╗██║"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&9   ╚██████╗██║  ██║██║ ╚████║██║ ╚████║╚██████╔╝██║ ╚████║"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&9    ╚═════╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═══╝ ╚═════╝ ╚═╝  ╚═══╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3         Orbital Strike Cannon"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6         Version " + getDescription().getVersion()));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b         Development by NguyenDevs"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
    }
}
