package com.nyssan.client.ui.screen;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.module.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NyssanMenuScreen extends Screen {
    private static final int BACKGROUND_COLOR = 0x88000000;
    private static final int PANEL_COLOR = 0xCC0a0a0a;
    private static final int BORDER_COLOR = 0xFF444444;
    private static final int TITLE_COLOR = 0x8B0000;
    private static final int TRIBUTE_COLOR = 0x6A0DAD;
    private static final int ACTIVE_COLOR = 0xFF6B6B;
    private static final int INACTIVE_COLOR = 0x444444;
    private static final int HOVER_BG = 0x22FFFFFF;

    private int panelX, panelY, panelWidth, panelHeight;
    private List<Module> modules;
    private int scrollOffset = 0;
    private int hoveredModuleIndex = -1;
    private double lastMouseX, lastMouseY;

    public NyssanMenuScreen() {
        super(Text.literal("NYSSAN"));
    }

    @Override
    protected void init() {
        super.init();
        panelWidth = Math.min(320, this.width - 40);
        panelHeight = Math.min(280, this.height - 40);
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        if (NyssanClient.moduleManager != null) {
            modules = new ArrayList<>(NyssanClient.moduleManager.getModules());
        } else {
            modules = new ArrayList<>();
            com.nyssan.client.NyssanClient.LOGGER.warn("ModuleManager is null when opening menu!");
        }
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    @Override
    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        super.render(context, mouseX, mouseY, delta);

        // Background
        context.fill(0, 0, this.width, this.height, BACKGROUND_COLOR);

        // Panel
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL_COLOR);
        drawBorder(context, panelX, panelY, panelWidth, panelHeight, BORDER_COLOR);

        // Title
        context.drawText(textRenderer, Text.literal("NYSSAN"), panelX + 14, panelY + 10, TITLE_COLOR, true);
        context.drawText(textRenderer, Text.literal("v" + NyssanClient.VERSION), panelX + 90, panelY + 12, 0x666666, true);

        // Tribute
        String tribute = "In Memory of Technoblade";
        context.drawText(textRenderer, Text.literal(tribute),
            panelX + panelWidth - textRenderer.getWidth(tribute) - 14,
            panelY + panelHeight - 20, TRIBUTE_COLOR, true);

        // Separator
        context.fill(panelX + 10, panelY + 26, panelX + panelWidth - 10, panelY + 27, 0x333333);

        // Search bar hint
        context.drawText(textRenderer, Text.literal("[Search modules...]"), panelX + 14, panelY + 30, 0x444444, true);

        // Module list
        int startY = panelY + 42;
        int moduleHeight = 26;
        int maxVisible = Math.max(1, (panelHeight - 72) / moduleHeight);

        hoveredModuleIndex = -1;
        for (int i = 0; i < Math.min(modules.size(), maxVisible); i++) {
            int actualIndex = i + scrollOffset;
            if (actualIndex >= modules.size()) break;

            Module mod = modules.get(actualIndex);
            int y = startY + i * moduleHeight;

            boolean hovered = mouseX >= panelX + 8 && mouseX <= panelX + panelWidth - 8
                && mouseY >= y && mouseY <= y + moduleHeight;

            if (hovered) {
                hoveredModuleIndex = actualIndex;
                context.fill(panelX + 6, y + 1, panelX + panelWidth - 6, y + moduleHeight - 1, HOVER_BG);
            }

            // Toggle indicator
            int dotX = panelX + 12;
            int dotY = y + 8;
            context.fill(dotX, dotY, dotX + 8, dotY + 8, mod.isEnabled() ? ACTIVE_COLOR : INACTIVE_COLOR);

            // Name
            context.drawText(textRenderer, Text.literal(mod.getName()), panelX + 26, y + 4,
                mod.isEnabled() ? 0xEEEEEE : 0x888888, true);

            // Description
            context.drawText(textRenderer, Text.literal(mod.getDescription()), panelX + 26, y + 14, 0x555555, true);

            // Status
            String status = mod.isEnabled() ? "\u2713" : "\u2717";
            int statusColor = mod.isEnabled() ? ACTIVE_COLOR : INACTIVE_COLOR;
            int sx = panelX + panelWidth - 30;
            context.drawText(textRenderer, Text.literal(status), sx, y + 4, statusColor, true);
        }
    }

    @Override
    public boolean mouseClicked(Click mouseClick, boolean used) {
        if (hoveredModuleIndex >= 0 && hoveredModuleIndex < modules.size()) {
            modules.get(hoveredModuleIndex).toggle();
            NyssanClient.configManager.save();
            return true;
        }
        return super.mouseClicked(mouseClick, used);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int moduleHeight = 26;
        int maxVisible = Math.max(1, (panelHeight - 72) / moduleHeight);
        int maxScroll = Math.max(0, modules.size() - maxVisible);
        scrollOffset = (int) Math.max(0, Math.min(scrollOffset - verticalAmount, maxScroll));
        return true;
    }
}
