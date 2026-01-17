package com.NguyenDevs.orbitalstrike.commands;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.cannon.Cannon;
import com.NguyenDevs.orbitalstrike.cannon.CannonRecipeManager;
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
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.NguyenDevs.orbitalstrike.cannon.CannonRecipeManager.CANNON_KEY;
import static com.NguyenDevs.orbitalstrike.cannon.CannonRecipeManager.DURABILITY_KEY;

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
            case "set":
                handleSet(sender, args);
                break;
            case "info":
                handleInfo(sender, args);
                break;
            default:
                sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "/cannon <create|remove|list|fire|target|info|give|reload|set>"));
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
            sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "/cannon fire <cannon>"));
            playErrorSound(sender);
            return;
        }

        String cannonName = args[1];
        Cannon cannon = plugin.getCannonManager().getCannon(cannonName);
        if (cannon == null) {
            sender.sendMessage(plugin.getMessageManager().getMessage("cannon.not-found", "%name%", cannonName));
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
        StrikeData strikeData = new StrikeData(cannon.getPayloadType());

        plugin.getPayloadManager().initiateStrike(cannon, strikeData, target);
        sender.sendMessage(plugin.getMessageManager().getMessage("cannon.fired",
                "%x%", String.valueOf(target.getBlockX()),
                "%y%", String.valueOf(target.getBlockY()),
                "%z%", String.valueOf(target.getBlockZ())
        ));
        playSound(sender);
    }

    private void handleTarget(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "/cannon target <cannon> <x> <y> <z>"));
            playErrorSound(sender);
            return;
        }

        String cannonName = args[1];
        Cannon cannon = plugin.getCannonManager().getCannon(cannonName);
        if (cannon == null) {
            sender.sendMessage(plugin.getMessageManager().getMessage("cannon.not-found", "%name%", cannonName));
            playErrorSound(sender);
            return;
        }

        double x, y, z;
        try {
            x = Double.parseDouble(args[2]);
            y = Double.parseDouble(args[3]);
            z = Double.parseDouble(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "Coordinates must be numbers"));
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

        StrikeData strikeData = new StrikeData(cannon.getPayloadType());

        plugin.getPayloadManager().initiateStrike(cannon, strikeData, target);
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

        ItemStack item = new ItemStack(cannon.getItemMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (cannon.isDurabilityEnabled()) {
                meta.getPersistentDataContainer().set(DURABILITY_KEY, PersistentDataType.INTEGER, 0);
            }

            meta.setDisplayName(ColorUtils.colorize(plugin.getMessageManager().getMessage("tool.name") + " (" + cannon.getName() + ")"));

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

            item.setItemMeta(meta);
        }

        targetPlayer.getInventory().addItem(item);
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

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "/cannon set <cannon> <parameter> <value>"));
            playErrorSound(sender);
            return;
        }

        String cannonName = args[1];
        Cannon cannon = plugin.getCannonManager().getCannon(cannonName);
        if (cannon == null) {
            sender.sendMessage(plugin.getMessageManager().getMessage("cannon.not-found", "%name%", cannonName));
            playErrorSound(sender);
            return;
        }

        String parameter = args[2].toLowerCase();
        String valueStr = args[3];
        Object value = null;

        try {
            if (valueStr.contains(".")) {
                value = Double.parseDouble(valueStr);
            } else {
                value = Integer.parseInt(valueStr);
            }
        } catch (NumberFormatException e) {
            // Try boolean
            if (valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("false")) {
                value = Boolean.parseBoolean(valueStr);
            } else {
                // Try string or material if needed, but for now let's assume numbers/booleans for params
                // Actually, we might want to set item material
                if (parameter.equals("material")) {
                    try {
                        value = Material.valueOf(valueStr.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "Invalid material"));
                        playErrorSound(sender);
                        return;
                    }
                } else {
                    sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "Value must be a number or boolean"));
                    playErrorSound(sender);
                    return;
                }
            }
        }
        
        // Handle special parameters
        if (parameter.equals("material")) {
            if (value instanceof Material) {
                cannon.setItemMaterial((Material) value);
            }
        } else if (parameter.equals("durability")) {
            if (value instanceof Boolean) {
                cannon.setDurabilityEnabled((Boolean) value);
            }
        } else if (parameter.equals("max-durability")) {
            if (value instanceof Integer) {
                cannon.setMaxDurability((Integer) value);
            }
        } else if (parameter.equals("cooldown")) {
            if (value instanceof Integer) {
                cannon.setCooldown((Integer) value);
            }
        } else {
            cannon.setParameter(parameter, value);
        }
        
        plugin.getCannonManager().saveCannons();
        sender.sendMessage(ColorUtils.colorize("&aSet parameter &e" + parameter + "&a to &e" + value + "&a for cannon &e" + cannonName));
        playSound(sender);
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getMessage("invalid-args", "%usage%", "/cannon info <cannon>"));
            playErrorSound(sender);
            return;
        }

        String cannonName = args[1];
        Cannon cannon = plugin.getCannonManager().getCannon(cannonName);
        if (cannon == null) {
            sender.sendMessage(plugin.getMessageManager().getMessage("cannon.not-found", "%name%", cannonName));
            playErrorSound(sender);
            return;
        }

        sender.sendMessage(plugin.getMessageManager().getMessage("cannon.info.header", "%name%", cannon.getName()));
        sender.sendMessage(plugin.getMessageManager().getMessage("cannon.info.payload", "%payload%", cannon.getPayloadType().name()));
        
        sender.sendMessage(ColorUtils.colorize("&eItem Settings:"));
        sender.sendMessage(ColorUtils.colorize("  &7Material: &f" + cannon.getItemMaterial().name()));
        sender.sendMessage(ColorUtils.colorize("  &7Durability: &f" + cannon.isDurabilityEnabled()));
        if (cannon.isDurabilityEnabled()) {
            sender.sendMessage(ColorUtils.colorize("  &7Max Durability: &f" + cannon.getMaxDurability()));
        }
        sender.sendMessage(ColorUtils.colorize("  &7Cooldown: &f" + cannon.getCooldown()));
        
        sender.sendMessage(plugin.getMessageManager().getMessage("cannon.info.parameters-header"));

        for (Map.Entry<String, Object> entry : cannon.getParameters().entrySet()) {
            sender.sendMessage(plugin.getMessageManager().getMessage("cannon.info.parameter",
                    "%key%", entry.getKey(),
                    "%value%", String.valueOf(entry.getValue())
            ));
        }
        playSound(sender);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "remove", "list", "fire", "target", "give", "reload", "set", "info");
        }

        if (args.length == 2) {
            if (Arrays.asList("remove", "set", "fire", "target", "info").contains(args[0].toLowerCase())) {
                return new ArrayList<>(plugin.getCannonManager().getCannons().keySet());
            }
            if (args[0].equalsIgnoreCase("give")) {
                return null;
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
            if (args[0].equalsIgnoreCase("set")) {
                String cannonName = args[1];
                Cannon cannon = plugin.getCannonManager().getCannon(cannonName);
                if (cannon != null) {
                    List<String> params = new ArrayList<>(cannon.getParameters().keySet());
                    params.add("material");
                    params.add("durability");
                    params.add("max-durability");
                    params.add("cooldown");
                    return params;
                }
            }
        }

        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("target")) {
                return Collections.emptyList();
            }
            if (args[0].equalsIgnoreCase("set")) {
                if (args[2].equalsIgnoreCase("material")) {
                    return Arrays.stream(Material.values())
                            .map(Enum::name)
                            .collect(Collectors.toList());
                }
                if (args[2].equalsIgnoreCase("durability")) {
                    return Arrays.asList("true", "false");
                }
            }
        }

        if (args.length == 5 && args[0].equalsIgnoreCase("target")) {
            return Collections.emptyList();
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