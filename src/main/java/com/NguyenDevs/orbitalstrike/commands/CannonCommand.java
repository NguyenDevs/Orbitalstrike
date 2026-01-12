package com.NguyenDevs.orbitalstrike.commands;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.cannon.Cannon;
import com.NguyenDevs.orbitalstrike.utils.ColorUtils;
import com.NguyenDevs.orbitalstrike.utils.PayloadType;
import com.NguyenDevs.orbitalstrike.utils.StrikeData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.NguyenDevs.orbitalstrike.cannon.CannonRecipeManager.CANNON_KEY;

public class CannonCommand implements CommandExecutor, TabCompleter {

    private final OrbitalStrike plugin;

    public CannonCommand(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Tất cả lệnh đều yêu cầu quyền admin
        if (!sender.hasPermission("orbitalstrike.admin")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            playErrorSound(sender);
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreate(sender, args);
                break;
            case "remove":
                handleRemove(sender, args);
                break;
            case "list":
                handleList(sender);
                break;
            case "fire":
                handleFire(sender, args);
                break;
            case "target":
                handleTarget(sender, args);
                break;
            case "give":
                handleGive(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "/cannon <create|remove|list|fire|target|info|give|reload>"));
                playErrorSound(sender);
                break;
        }

        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "/cannon create <name> [payload]"));
            playErrorSound(sender);
            return;
        }

        String name = args[1];

        if (plugin.getCannonManager().getCannon(name) != null) {
            sender.sendMessage(plugin.getMessageManager().getMessage("error.cannon-exists"));
            playErrorSound(sender);
            return;
        }

        PayloadType payloadType = PayloadType.STAB;
        if (args.length >= 3) {
            try {
                payloadType = PayloadType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(plugin.getMessageManager().getMessage("payload.invalid"));
                playErrorSound(sender);
                return;
            }
        }

        plugin.getCannonManager().createCannon(name, payloadType);
        sender.sendMessage(plugin.getMessageManager().getMessage("cannon.created", "%name%", name));
        playSound(sender);
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "/cannon remove <name>"));
            playErrorSound(sender);
            return;
        }

        String name = args[1];
        if (plugin.getCannonManager().getCannon(name) == null) {
            sender.sendMessage(plugin.getMessageManager().getMessage("cannon.not-found", "%name%", name));
            playErrorSound(sender);
            return;
        }

        plugin.getCannonManager().removeCannon(name);
        sender.sendMessage(plugin.getMessageManager().getMessage("cannon.removed", "%name%", name));
        playSound(sender);
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(plugin.getMessageManager().getMessage("cannon.list-header"));
        for (Cannon cannon : plugin.getCannonManager().getCannons().values()) {
            String message = plugin.getMessageManager().getMessage("cannon.list-item",
                    "%name%", cannon.getName(),
                    "%payload%", cannon.getPayloadType().name()
            );

            if (sender instanceof Player) {
                TextComponent component = new TextComponent(message);
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cannon give " + sender.getName() + " " + cannon.getName()));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to get tool").create()));
                ((Player) sender).spigot().sendMessage(component);
            } else {
                sender.sendMessage(message);
            }
        }
        playSound(sender);
    }

    private void handleFire(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("player-only"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "/cannon fire <payload>"));
            playErrorSound(sender);
            return;
        }

        String payloadStr = args[1].toUpperCase();
        PayloadType payloadType;
        try {
            payloadType = PayloadType.valueOf(payloadStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getMessageManager().getMessage("payload.invalid"));
            playErrorSound(sender);
            return;
        }

        Player player = (Player) sender;

        if (!plugin.getConfigManager().getEnabledWorlds().contains(player.getWorld().getName())) {
            sender.sendMessage(plugin.getMessageManager().getMessage("error.world-disabled"));
            playErrorSound(sender);
            return;
        }

        Location target = player.getTargetBlock(null, 100).getLocation();

        Cannon dummyCannon = new Cannon("ADMIN_FIRE", payloadType);
        StrikeData strikeData = new StrikeData(payloadType);

        plugin.getPayloadManager().initiateStrike(dummyCannon, strikeData, target);
        sender.sendMessage(plugin.getMessageManager().getMessage("cannon.fired",
                "%x%", String.valueOf(target.getBlockX()),
                "%y%", String.valueOf(target.getBlockY()),
                "%z%", String.valueOf(target.getBlockZ())
        ));
        playSound(sender);
    }

    private void handleTarget(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "/cannon target <x> <y> <z> <payload>"));
            playErrorSound(sender);
            return;
        }

        double x, y, z;
        try {
            x = Double.parseDouble(args[1]);
            y = Double.parseDouble(args[2]);
            z = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "Coordinates must be numbers"));
            playErrorSound(sender);
            return;
        }

        String payloadStr = args[4].toUpperCase();
        PayloadType payloadType;
        try {
            payloadType = PayloadType.valueOf(payloadStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getMessageManager().getMessage("payload.invalid"));
            playErrorSound(sender);
            return;
        }

        Location target;
        if (sender instanceof Player) {
            target = new Location(((Player) sender).getWorld(), x, y, z);
        } else {
            target = new Location(plugin.getServer().getWorlds().get(0), x, y, z);
        }

        if (!plugin.getConfigManager().getEnabledWorlds().contains(target.getWorld().getName())) {
            sender.sendMessage(plugin.getMessageManager().getMessage("error.world-disabled"));
            playErrorSound(sender);
            return;
        }

        Cannon dummyCannon = new Cannon("ADMIN_TARGET", payloadType);
        StrikeData strikeData = new StrikeData(payloadType);

        plugin.getPayloadManager().initiateStrike(dummyCannon, strikeData, target);
        sender.sendMessage(plugin.getMessageManager().getMessage("cannon.fired",
                "%x%", String.valueOf(target.getBlockX()),
                "%y%", String.valueOf(target.getBlockY()),
                "%z%", String.valueOf(target.getBlockZ())
        ));
        playSound(sender);
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "/cannon give <player> <cannon>"));
            playErrorSound(sender);
            return;
        }

        Player targetPlayer = plugin.getServer().getPlayer(args[1]);
        if (targetPlayer == null) {
            sender.sendMessage(plugin.getMessageManager().getMessage("error.player-not-found"));
            playErrorSound(sender);
            return;
        }

        String cannonName = args[2];
        Cannon cannon = plugin.getCannonManager().getCannon(cannonName);
        if (cannon == null) {
            sender.sendMessage(plugin.getMessageManager().getMessage("cannon.not-found", "%name%", cannonName));
            playErrorSound(sender);
            return;
        }

        ItemStack rod = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = rod.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            damageable.setDamage(rod.getType().getMaxDurability() - 1);

            meta.setDisplayName(plugin.getMessageManager().getMessage("tool.name"));

            List<String> loreConfig = plugin.getMessageManager().getMessageList("tool.lore");
            List<String> finalLore = new ArrayList<>();
            for (String line : loreConfig) {
                finalLore.add(ColorUtils.colorize(line
                        .replace("%cannon%", cannon.getName())
                        .replace("%payload%", cannon.getPayloadType().name())
                ));
            }
            meta.setLore(finalLore);

            meta.getPersistentDataContainer().set(CANNON_KEY, PersistentDataType.STRING, cannon.getName());

            rod.setItemMeta(meta);
        }

        targetPlayer.getInventory().addItem(rod);
        sender.sendMessage(plugin.getMessageManager().getMessage("cannon.given", "%name%", cannonName));
        playSound(sender);
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().loadConfig();
        plugin.getMessageManager().loadMessages();
        plugin.getCannonManager().loadCannons();
        plugin.getCannonRecipeManager().registerRecipes();
        sender.sendMessage(plugin.getMessageManager().getMessage("reload.success"));
        playSound(sender);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "remove", "list", "fire", "target", "give", "reload");
        }

        if (args.length == 2) {
            if (Arrays.asList("remove").contains(args[0].toLowerCase())) {
                return new ArrayList<>(plugin.getCannonManager().getCannons().keySet());
            }
            if (args[0].equalsIgnoreCase("give")) {
                return null;
            }
            if (args[0].equalsIgnoreCase("fire")) {
                return Arrays.stream(PayloadType.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("target")) {
                return Collections.emptyList();
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create")) {
                return Arrays.stream(PayloadType.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("give")) {
                return new ArrayList<>(plugin.getCannonManager().getCannons().keySet());
            }
            if (args[0].equalsIgnoreCase("target")) {
                return Collections.emptyList();
            }
        }

        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("target")) {
                return Collections.emptyList();
            }
        }

        if (args.length == 5 && args[0].equalsIgnoreCase("target")) {
            return Arrays.stream(PayloadType.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        }

        return null;
    }

    private void playSound(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            try {
                player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.0f, 1.5f);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error playing success sound for player: " + player.getName(), e);
            }
        }
    }

    private void playErrorSound(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            try {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error playing error sound for player: " + player.getName(), e);
            }
        }
    }
}