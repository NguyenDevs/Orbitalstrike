package com.NguyenDevs.orbitalstrike.utils;

import net.md_5.bungee.api.ChatColor;
import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:#([A-Fa-f0-9]{6}):#([A-Fa-f0-9]{6})>(.*?)</gradient>");

    public static String colorize(String message) {
        if (message == null) return "";

        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (gradientMatcher.find()) {
            String startHex = gradientMatcher.group(1);
            String endHex = gradientMatcher.group(2);
            String content = gradientMatcher.group(3);
            gradientMatcher.appendReplacement(buffer, applyGradient(content, startHex, endHex));
        }
        gradientMatcher.appendTail(buffer);
        message = buffer.toString();

        Matcher hexMatcher = HEX_PATTERN.matcher(message);
        buffer = new StringBuffer();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(buffer, ChatColor.of("#" + hexMatcher.group(1)).toString());
        }
        hexMatcher.appendTail(buffer);
        
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private static String applyGradient(String text, String startHex, String endHex) {
        Color start = new Color(Integer.parseInt(startHex, 16));
        Color end = new Color(Integer.parseInt(endHex, 16));
        StringBuilder sb = new StringBuilder();
        int length = text.length();
        for (int i = 0; i < length; i++) {
            float ratio = (length > 1) ? (float) i / (length - 1) : 0;
            int red = (int) (start.getRed() * (1 - ratio) + end.getRed() * ratio);
            int green = (int) (start.getGreen() * (1 - ratio) + end.getGreen() * ratio);
            int blue = (int) (start.getBlue() * (1 - ratio) + end.getBlue() * ratio);
            sb.append(ChatColor.of(new Color(red, green, blue)));
            sb.append(text.charAt(i));
        }
        return sb.toString();
    }
}
