package com.nyssan.client.ui;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HudRenderer {
    private static final int MODULE_COLOR = 0x00FF88;
    private static final int TEXT_COLOR   = 0xFFFFFF;
    private static final int BG_COLOR     = 0x80000000;
    private static final int BAR_COLOR    = 0xFF1A6FCC;
    private static final int HUD_MARGIN   = 3;
    private static final int LINE_HEIGHT  = 11;

    public static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        int sw = mc.getWindow().getScaledWidth();

        // Módulos ativos — estilo LB (right-aligned, ordenados por tamanho)
        int leftY = HUD_MARGIN;
        List<Module> active = NyssanClient.moduleManager.getModules().stream()
            .filter(Module::isEnabled)
            .filter(m -> m.getCategory() != Module.Category.OPTIMIZATION)
            .sorted(Comparator.<Module, Integer>comparing(m -> mc.textRenderer.getWidth(m.getName())).reversed())
            .collect(Collectors.toList());

        for (Module mod : active) {
            String name = mod.getName();
            int tw = mc.textRenderer.getWidth(name);
            int x = sw - HUD_MARGIN - 2 - tw;

            // Fundo
            ctx.fill(x - 1, leftY - 1, x + tw + 1, leftY + LINE_HEIGHT - 1, BG_COLOR);
            // Barra lateral
            ctx.fill(x + tw + 1, leftY - 1, x + tw + 3, leftY + LINE_HEIGHT - 1, BAR_COLOR);
            // Texto
            ctx.drawText(mc.textRenderer, Text.literal(name), x, leftY, TEXT_COLOR, true);

            leftY += LINE_HEIGHT + 1;
        }

        // FPS
        int fpsY = HUD_MARGIN;
        String fps = mc.getCurrentFps() + " FPS";
        int fw = mc.textRenderer.getWidth(fps);
        int fx = sw - HUD_MARGIN - 2;
        ctx.fill(fx - fw - 4, fpsY - 1, fx, fpsY + LINE_HEIGHT - 1, BG_COLOR);
        ctx.drawText(mc.textRenderer, Text.literal(fps), fx - fw - 2, fpsY, TEXT_COLOR, true);

        // Ping
        if (mc.player != null && mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            int ping = entry != null ? entry.getLatency() : 0;
            String pingText = ping + "ms";
            int pw = mc.textRenderer.getWidth(pingText);
            int pingY = fpsY + LINE_HEIGHT + 2;
            ctx.fill(fx - pw - 4, pingY - 1, fx, pingY + LINE_HEIGHT - 1, BG_COLOR);
            ctx.drawText(mc.textRenderer, Text.literal(pingText), fx - pw - 2, pingY, TEXT_COLOR, true);
        }

        // Watermark
        String wm = "NYSSAN v" + NyssanClient.VERSION;
        int wmW = mc.textRenderer.getWidth(wm);
        int wmY = mc.getWindow().getScaledHeight() - LINE_HEIGHT - 2;
        ctx.fill(fx - wmW - 4, wmY - 1, fx, wmY + LINE_HEIGHT - 1, 0x60000000);
        ctx.drawText(mc.textRenderer, Text.literal(wm), fx - wmW - 2, wmY, 0xAAAACC, true);
    }
}
