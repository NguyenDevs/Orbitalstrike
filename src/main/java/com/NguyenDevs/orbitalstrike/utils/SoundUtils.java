package com.NguyenDevs.orbitalstrike.utils;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class SoundUtils {

    private SoundUtils() {}

    public static void playSuccessSound(CommandSender sender, Logger logger) {
        if (sender instanceof Player player) {
            try {
                player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.0f, 1.5f);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error playing success sound for player: " + player.getName(), e);
            }
        }
    }

    public static void playErrorSound(CommandSender sender, Logger logger) {
        if (sender instanceof Player player) {
            try {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error playing error sound for player: " + player.getName(), e);
            }
        }
    }
}
