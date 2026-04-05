package com.nyssan.client.ui;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.module.Module;
import com.nyssan.client.module.impl.movement.FreeLook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.List;

public class ClientGUI {
    private static final int HUD_MARGIN_X = 4;
    private static final int HUD_MARGIN_Y = 4;
    private static final int LINE_HEIGHT = 10;
    private static final int ACTIVE_MODULE_COLOR = 0x00FF88;

    public static void render(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        // Left side - active modules
        int leftX = HUD_MARGIN_X;
        int leftY = HUD_MARGIN_Y;
        List<Module> modules = NyssanClient.moduleManager.getModules();
        for (Module mod : modules) {
            if (mod.isEnabled() && mod.getCategory() != Module.Category.OPTIMIZATION) {
                String name = mod.getName();
                int tw = mc.textRenderer.getWidth(name);
                context.fill(leftX - 2, leftY - 1, leftX + tw + 4, leftY + LINE_HEIGHT - 1, 0x80000000);
                context.drawText(mc.textRenderer, Text.literal(name), leftX, leftY, ACTIVE_MODULE_COLOR, true);
                leftY += LINE_HEIGHT + 1;
            }
        }

        // FreeLook indicator
        FreeLook freeLook = (FreeLook) NyssanClient.moduleManager.getModule(FreeLook.class).orElse(null);
        if (freeLook != null && freeLook.isEnabled()) {
            String text = "[FL]";
            int tw = mc.textRenderer.getWidth(text);
            context.fill(leftX - 2, leftY - 1, leftX + tw + 4, leftY + LINE_HEIGHT - 1, 0x80226622);
            context.drawText(mc.textRenderer, Text.literal(text), leftX, leftY, 0x44FF44, true);
        }

        // Right side - FPS & Ping
        int rightX = sw - HUD_MARGIN_X;
        int rightY = HUD_MARGIN_Y;
        String fps = mc.getCurrentFps() + " FPS";
        int fpsW = mc.textRenderer.getWidth(fps);
        context.fill(rightX - fpsW - 4, rightY - 1, rightX, rightY + LINE_HEIGHT - 1, 0x80000000);
        context.drawText(mc.textRenderer, Text.literal(fps), rightX - fpsW - 2, rightY, 0xFFFFFF, true);

        if (mc.player != null && mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            int ping = entry != null ? entry.getLatency() : 0;
            String pingText = ping + "ms";
            int pw = mc.textRenderer.getWidth(pingText);
            int pingY = rightY + LINE_HEIGHT + 2;
            context.fill(rightX - pw - 4, pingY - 1, rightX, pingY + LINE_HEIGHT - 1, 0x80000000);
            context.drawText(mc.textRenderer, Text.literal(pingText), rightX - pw - 2, pingY, 0xFFFFFF, true);
        }

        // NYSSAN watermark
        String watermark = "NYSSAN v" + NyssanClient.VERSION;
        int wmW = mc.textRenderer.getWidth(watermark);
        int wmY = sh - LINE_HEIGHT - 2;
        context.fill(rightX - wmW - 4, wmY - 1, rightX, wmY + LINE_HEIGHT - 1, 0x60000000);
        context.drawText(mc.textRenderer, Text.literal(watermark), rightX - wmW - 2, wmY, 0xAAAACC, true);
    }
}
