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
    // Colors - dark theme
    private static final int BG_PRIMARY   = 0xDD0A0A0A;
    private static final int BG_SECONDARY  = 0xCC0F0F0F;
    private static final int BG_HOVER      = 0x33333333;
    private static final int ACCENT        = 0xFF4488CC;
    private static final int WHITE         = 0xEEEEEE;
    private static final int GRAY          = 0x888888;
    private static final int ON_COLOR      = 0x44FF44;
    private static final int OFF_COLOR     = 0x666666;
    private static final int BORDER_COLOR  = 0xFF333355;
    private static final int SEPARATOR     = 0x22FFFFFF;

    // Panel data
    private final List<CategoryPanel> categoryPanels = new ArrayList<>();
    private CategoryPanel draggingPanel = null;
    private double dragOffsetX, dragOffsetY;
    private int mouseX = 0, mouseY = 0;
    private static final int PANEL_HEADER_HEIGHT = 22;
    private static final int PANEL_WIDTH = 120;
    private static final int PANEL_SPACING = 10;
    private static final int PANEL_START_X = 10;
    private static final int PANEL_START_Y = 30;

    public NewClickScreen() {
        super(Text.literal("NYSSAN Client"));
    }

    @Override
    protected void init() {
        super.init();
        buildPanels();
    }

    private void buildPanels() {
        categoryPanels.clear();
        Map<Module.Category, List<Module>> categories = new LinkedHashMap<>();
        for (Module m : NyssanClient.moduleManager.getModules()) {
            categories.computeIfAbsent(m.getCategory(), k -> new ArrayList<>()).add(m);
        }

        // Grid layout: calculate scaled dimensions
        int sw = this.width;
        int col = 0;
        int row = 0;
        int maxX = PANEL_START_X;

        for (Map.Entry<Module.Category, List<Module>> entry : categories.entrySet()) {
            int px = PANEL_START_X + col * (PANEL_WIDTH + PANEL_SPACING);
            int py = PANEL_START_Y + row * 200;

            CategoryPanel panel = new CategoryPanel(entry.getKey(), entry.getValue(), px, py, PANEL_WIDTH);
            categoryPanels.add(panel);

            maxX = Math.max(maxX, px + PANEL_WIDTH);
            col++;

            // Wrap to next row if exceeds screen width
            if (PANEL_START_X + (col + 1) * (PANEL_WIDTH + PANEL_SPACING) > sw) {
                col = 0;
                row++;
            }
        }
    }

    @Override
    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        super.render(context, mouseX, mouseY, delta);

        context.fill(0, 0, this.width, this.height, BG_PRIMARY);

        // Title bar
        context.drawText(textRenderer, Text.literal("NYSSAN Client v" + NyssanClient.VERSION), 12, 8, ACCENT, true);
        context.fill(4, 22, this.width - 4, 23, ACCENT);

        for (CategoryPanel panel : categoryPanels) {
            panel.render(context, mouseX, mouseY);
        }
    }

    private CategoryPanel getPanelAt(double mx, double my) {
        for (int i = categoryPanels.size() - 1; i >= 0; i--) {
            CategoryPanel p = categoryPanels.get(i);
            if (mx >= p.x && mx <= p.x + p.width && my >= p.y && my <= p.y + PANEL_HEADER_HEIGHT) {
                return p;
            }
        }
        return null;
    }

    private Module getModuleAt(double mx, double my) {
        for (CategoryPanel panel : categoryPanels) {
            Module m = panel.getModuleAt(mx, my);
            if (m != null) return m;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(Click mouseClick, boolean used) {
        // Check module click first
        Module mod = getModuleAt(mouseX, mouseY);
        if (mod != null) {
            mod.toggle();
            NyssanClient.configManager.save();
            return true;
        }

        // Check panel drag
        CategoryPanel panel = getPanelAt(mouseX, mouseY);
        if (panel != null) {
            draggingPanel = panel;
            dragOffsetX = mouseX - panel.x;
            dragOffsetY = mouseY - panel.y;
            // Move to front
            categoryPanels.remove(panel);
            categoryPanels.add(panel);
            return true;
        }

        return super.mouseClicked(mouseClick, used);
    }

    @Override
    public boolean mouseReleased(Click mouseClick) {
        draggingPanel = null;
        return super.mouseReleased(mouseClick);
    }

    @Override
    public boolean mouseDragged(Click mouseClick, double deltaX, double deltaY) {
        if (draggingPanel != null) {
            draggingPanel.x = mouseX - (int) dragOffsetX;
            draggingPanel.y = mouseY - (int) dragOffsetY;
            return true;
        }
        return super.mouseDragged(mouseClick, deltaX, deltaY);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    // Category Panel inner class
    private static class CategoryPanel {
        final Module.Category category;
        final List<Module> modules;
        int x, y;
        final int width;

        CategoryPanel(Module.Category category, List<Module> modules, int x, int y, int width) {
            this.category = category;
            this.modules = modules;
            this.x = x;
            this.y = y;
            this.width = width;
        }

        void render(DrawContext ctx, int mx, int my) {
            int totalHeight = PANEL_HEADER_HEIGHT + modules.size() * 20 + 4;

            // Header background
            ctx.fill(x, y, x + width, y + PANEL_HEADER_HEIGHT, ACCENT);
            ctx.drawText(getTr(), Text.literal(category.displayName.toUpperCase()), x + 6, y + 6, WHITE, true);

            // Module list
            for (int i = 0; i < modules.size(); i++) {
                Module mod = modules.get(i);
                int modY = y + PANEL_HEADER_HEIGHT + i * 20;
                boolean hovered = mx >= x + 2 && mx <= x + width - 2 && my >= modY && my <= modY + 19;
                int bgColor = hovered ? BG_HOVER : BG_SECONDARY;
                ctx.fill(x + 2, modY, x + width - 2, modY + 18, bgColor);

                // Module name
                int nameColor = mod.isEnabled() ? ON_COLOR : GRAY;
                ctx.drawText(getTr(), Text.literal(mod.getName()), x + 6, modY + 4, nameColor, true);

                // Toggle indicator
                String status = mod.isEnabled() ? "+" : "-";
                int statusColor = mod.isEnabled() ? ON_COLOR : OFF_COLOR;
                int sw = getTr().getWidth(status);
                ctx.drawText(getTr(), Text.literal(status), x + width - 6 - sw, modY + 4, statusColor, true);

                // Separator
                ctx.fill(x + 4, modY + 18, x + width - 4, modY + 19, SEPARATOR);
            }

            // Border
            ctx.fill(x, y, x + width, y + 1, BORDER_COLOR);
            ctx.fill(x, y + totalHeight - 1, x + width, y + totalHeight, BORDER_COLOR);
            ctx.fill(x, y, x + 1, y + totalHeight, BORDER_COLOR);
            ctx.fill(x + width - 1, y, x + width, y + totalHeight, BORDER_COLOR);
        }

        Module getModuleAt(double mx, double my) {
            for (int i = 0; i < modules.size(); i++) {
                int modY = y + PANEL_HEADER_HEIGHT + i * 20;
                if (mx >= x + 2 && mx <= x + width - 2 && my >= modY && my <= modY + 18) {
                    return modules.get(i);
                }
            }
            return null;
        }

        private net.minecraft.client.font.TextRenderer getTr() {
            return net.minecraft.client.MinecraftClient.getInstance().textRenderer;
        }
    }
}
