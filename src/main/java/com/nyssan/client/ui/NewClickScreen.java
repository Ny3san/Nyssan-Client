package com.nyssan.client.ui;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.module.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NewClickScreen extends Screen {
    // Cores - estilo LiquidBounce
    private static final int BG           = 0xCC0D0D0D;
    private static final int PANEL_BG     = 0xDD111111;
    private static final int HEADER_BG    = 0xFF1A6FCC;
    private static final int HEADER_TEXT  = 0xFFFFFFFF;
    private static final int MOD_ACTIVE   = 0xFF1A6FCC;
    private static final int MOD_TEXT     = 0xFFEEEEEE;
    private static final int MOD_INACTIVE = 0xFF888888;
    private static final int MOD_HOVER    = 0xFF1D1D1D;
    private static final int ON_INDICATOR = 0xFF44FF44;
    private static final int SEPARATOR    = 0xFF222222;
    private static final int BORDER_COLOR = 0xFF1A6FCC;

    // Layout
    private static final int PANEL_W       = 130;
    private static final int HEADER_H      = 20;
    private static final int MODULE_H      = 16;
    private static final int PANEL_SPACING = 8;
    private static final int START_X       = 10;
    private static final int START_Y       = 30;

    private final List<CategoryPanel> panels = new ArrayList<>();
    private CategoryPanel dragging = null;
    private int dragOffX, dragOffY;
    private int mouseX = 0, mouseY = 0;

    public NewClickScreen() {
        super(Text.literal("NYSSAN Client"));
    }

    @Override
    protected void init() {
        super.init();
        panels.clear();

        Map<Module.Category, List<Module>> cats = new LinkedHashMap<>();
        for (Module m : NyssanClient.moduleManager.getModules()) {
            cats.computeIfAbsent(m.getCategory(), k -> new ArrayList<>()).add(m);
        }

        int i = 0;
        for (Map.Entry<Module.Category, List<Module>> e : cats.entrySet()) {
            int px = START_X + i * (PANEL_W + PANEL_SPACING);
            // Wrap to next row if off screen
            if (px + PANEL_W + 5 > this.width && i > 0) {
                i = 0;
                px = START_X;
            }
            panels.add(new CategoryPanel(e.getKey(), e.getValue(), px, START_Y));
            i++;
        }
    }

    @Override
    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        super.render(context, mouseX, mouseY, delta);

        // Fundo escuro
        context.fill(0, 0, this.width, this.height, BG);

        // Título + barra
        context.drawText(textRenderer, Text.literal("NYSSAN Client v" + NyssanClient.VERSION), 8, 8, HEADER_TEXT, true);
        context.fill(0, 22, this.width, 23, HEADER_BG);

        // Renderizar painéis
        for (CategoryPanel panel : panels) {
            panel.render(context, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(Click mc, boolean used) {
        int mx = mouseX, my = mouseY;

        // Verifica clique em módulo primeiro
        for (CategoryPanel panel : panels) {
            Module mod = panel.getModuleAt(mx, my);
            if (mod != null) {
                mod.toggle();
                NyssanClient.configManager.save();
                return true;
            }
        }

        // Verifica clique no header para arrastar
        for (int i = panels.size() - 1; i >= 0; i--) {
            CategoryPanel p = panels.get(i);
            if (mx >= p.x && mx <= p.x + p.w && my >= p.y && my <= p.y + HEADER_H) {
                dragging = p;
                dragOffX = mx - p.x;
                dragOffY = my - p.y;
                // Trazer para frente
                panels.remove(p);
                panels.add(p);
                return true;
            }
        }

        return super.mouseClicked(mc, used);
    }

    @Override
    public boolean mouseReleased(Click mc) {
        dragging = null;
        return super.mouseReleased(mc);
    }

    @Override
    public boolean mouseDragged(Click mc, double dx, double dy) {
        if (dragging != null) {
            dragging.x = mouseX - dragOffX;
            dragging.y = mouseY - dragOffY;
            return true;
        }
        return super.mouseDragged(mc, dx, dy);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    private static class CategoryPanel {
        final Module.Category cat;
        final List<Module> modules;
        int x, y, w = PANEL_W;

        CategoryPanel(Module.Category cat, List<Module> modules, int x, int y) {
            this.cat = cat;
            this.modules = modules;
            this.x = x;
            this.y = y;
        }

        void render(DrawContext ctx, int mx, int my) {
            int panelH = HEADER_H + modules.size() * MODULE_H + 4;

            // Sombra
            ctx.fill(x + 2, y + 2, x + w + 2, y + panelH + 2, 0x55000000);

            // Fundo do painel
            ctx.fill(x, y, x + w, y + panelH, PANEL_BG);

            // Header azul
            ctx.fill(x, y, x + w, y + HEADER_H, HEADER_BG);

            // Nome da categoria centralizado
            String name = cat.displayName.toUpperCase();
            int tw = getTr().getWidth(name);
            int tx = x + (w - tw) / 2;
            int ty = y + (HEADER_H - 8) / 2;
            ctx.drawText(getTr(), Text.literal(name), tx, ty, HEADER_TEXT, true);

            // Linha inferior do header
            ctx.fill(x, y + HEADER_H, x + w, y + HEADER_H + 1, 0xFF0D4A8A);

            // Módulos
            int modY = y + HEADER_H + 2;
            for (Module mod : modules) {
                boolean active = mod.isEnabled();
                boolean hovered = mx >= x && mx <= x + w && my >= modY && my <= modY + MODULE_H;

                if (hovered) {
                    ctx.fill(x, modY, x + w, modY + MODULE_H, MOD_HOVER);
                }

                // Barra lateral se ativo
                if (active) {
                    ctx.fill(x, modY, x + 2, modY + MODULE_H, MOD_ACTIVE);
                }

                // Nome do módulo
                int color = active ? MOD_ACTIVE : MOD_TEXT;
                ctx.drawText(getTr(), Text.literal(mod.getName()), x + 6, modY + 3, color, true);

                // Status ON/OFF
                String status = active ? "ON" : "OFF";
                int sc = active ? ON_INDICATOR : MOD_INACTIVE;
                int sw = getTr().getWidth(status);
                ctx.drawText(getTr(), Text.literal(status), x + w - sw - 6, modY + 3, sc, true);

                // Separador
                ctx.fill(x + 2, modY + MODULE_H - 1, x + w - 2, modY + MODULE_H, SEPARATOR);

                modY += MODULE_H;
            }

            // Bordas
            ctx.fill(x, y, x + w, y + 1, BORDER_COLOR);
            ctx.fill(x, y + panelH - 1, x + w, y + panelH, BORDER_COLOR);
            ctx.fill(x, y, x + 1, y + panelH, BORDER_COLOR);
            ctx.fill(x + w - 1, y, x + w, y + panelH, BORDER_COLOR);
        }

        Module getModuleAt(int mx, int my) {
            int modY = y + HEADER_H + 2;
            for (Module mod : modules) {
                if (mx >= x + 2 && mx <= x + w - 2 && my >= modY && my <= modY + MODULE_H) {
                    return mod;
                }
                modY += MODULE_H;
            }
            return null;
        }

        private net.minecraft.client.font.TextRenderer getTr() {
            return net.minecraft.client.MinecraftClient.getInstance().textRenderer;
        }
    }
}
