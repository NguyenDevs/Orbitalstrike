package com.NguyenDevs.orbitalstrike.configuration;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.utils.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class MessageManager {
    private final OrbitalStrike plugin;
    private FileConfiguration messageConfig;
    private File messageFile;

    private static final Set<String> MESSAGES_WITH_PREFIX = new HashSet<>();

    static {
        MESSAGES_WITH_PREFIX.add("cooldown");
        MESSAGES_WITH_PREFIX.add("no-permission");
        MESSAGES_WITH_PREFIX.add("no-permission-craft");
        MESSAGES_WITH_PREFIX.add("invalid-args");
        MESSAGES_WITH_PREFIX.add("reload.success");
        MESSAGES_WITH_PREFIX.add("cannon.created");
        MESSAGES_WITH_PREFIX.add("cannon.removed");
        MESSAGES_WITH_PREFIX.add("cannon.not-found");
        MESSAGES_WITH_PREFIX.add("cannon.given");
        MESSAGES_WITH_PREFIX.add("cannon.no-permission");
        MESSAGES_WITH_PREFIX.add("cannon.set-success");
        MESSAGES_WITH_PREFIX.add("payload.invalid");
        MESSAGES_WITH_PREFIX.add("error.world-disabled");
        MESSAGES_WITH_PREFIX.add("error.cannon-exists");
        MESSAGES_WITH_PREFIX.add("error.player-not-found");
        MESSAGES_WITH_PREFIX.add("error.player-only");
    }

    public MessageManager(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    public void loadMessages() {
        if (messageFile == null) {
            messageFile = new File(plugin.getDataFolder(), "messages.yml");
        }

        if (!messageFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messageConfig = YamlConfiguration.loadConfiguration(messageFile);

        InputStream defConfigStream = plugin.getResource("messages.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            messageConfig.setDefaults(defConfig);

            boolean changed = false;
            for (String key : defConfig.getKeys(true)) {
                if (!messageConfig.contains(key)) {
                    messageConfig.set(key, defConfig.get(key));
                    changed = true;
                }
            }

            if (changed) {
                saveMessages();
            }
        }
    }

    public void saveMessages() {
        try {
            messageConfig.save(messageFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save messages.yml!", e);
        }
    }

    public String getMessage(String key) {
        String message = messageConfig.getString(key);
        if (message == null) {
            return ColorUtils.colorize("&cMissing message: " + key);
        }
        if (shouldHavePrefix(key)) {
            String prefix = messageConfig.getString("prefix", "");
            message = prefix + message;
        }

        return ColorUtils.colorize(message);
    }

    public String getMessage(String key, String... placeholders) {
        String message = messageConfig.getString(key);
        if (message == null) {
            return ColorUtils.colorize("&cMissing message: " + key);
        }

        if (shouldHavePrefix(key)) {
            String prefix = messageConfig.getString("prefix", "");
            message = prefix + message;
        }

        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        return ColorUtils.colorize(message);
    }

    public List<String> getMessageList(String key) {
        if (!messageConfig.contains(key)) {
            return new ArrayList<>();
        }
        return messageConfig.getStringList(key);
    }

    private boolean shouldHavePrefix(String key) {
        return MESSAGES_WITH_PREFIX.contains(key);
    }
}