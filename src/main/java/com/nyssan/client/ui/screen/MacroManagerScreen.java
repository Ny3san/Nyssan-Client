package com.nyssan.client.ui.screen;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.macro.Macro;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MacroManagerScreen extends Screen {
    private static final int BACKGROUND_COLOR = 0x88000000;
    private static final int PANEL_COLOR = 0xCC0a0a0a;
    private static final int BORDER_COLOR = 0xFF444444;
    private static final int TITLE_COLOR = 0xFFFFFF;

    private int panelX, panelY, panelWidth, panelHeight;
    private List<Macro> macros;
    private String selectedMacroName = null;
    private int hoveredMacroIndex = -1;

    public MacroManagerScreen() {
        super(Text.literal("Macro Manager"));
    }

    @Override
    protected void init() {
        super.init();
        panelWidth = Math.min(360, this.width - 40);
        panelHeight = Math.min(320, this.height - 40);
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        macros = new ArrayList<>(NyssanClient.macroManager.getMacros());

        int btnX = panelX + panelWidth - 115;
        int btnY = panelY + 7;
        addDrawableChild(ButtonWidget.builder(Text.literal("+ New Macro"), btn -> {
            Macro newMacro = new Macro("New Macro", 0);
            NyssanClient.macroManager.addMacro(newMacro);
            NyssanClient.macroManager.saveMacros();
            refresh();
        }).dimensions(btnX, btnY, 100, 16).build());
    }

    private void refresh() {
        clearChildren();
        init();
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    @Override
    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.fill(0, 0, this.width, this.height, BACKGROUND_COLOR);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL_COLOR);
        drawBorder(context, panelX, panelY, panelWidth, panelHeight, BORDER_COLOR);

        // Title
        context.drawText(textRenderer, Text.literal("MACRO MANAGER"), panelX + 12, panelY + 10, TITLE_COLOR, true);

        // Separator
        context.fill(panelX + 10, panelY + 26, panelX + panelWidth - 10, panelY + 27, 0x333333);

        int startY = panelY + 34;
        int macroHeight = 28;
        int maxVisible = Math.max(1, (panelHeight - 80) / macroHeight);

        hoveredMacroIndex = -1;
        int detailY = panelY + panelHeight - 65;

        for (int i = 0; i < Math.min(macros.size(), Math.max(maxVisible, 1)); i++) {
            int y = startY + i * macroHeight;
            if (y + macroHeight > detailY - 5) break;

            Macro macro = macros.get(i);
            boolean selected = selectedMacroName != null && selectedMacroName.equals(macro.getName());
            boolean hovered = mouseX >= panelX + 8 && mouseX <= panelX + panelWidth - 8
                && mouseY >= y && mouseY <= y + macroHeight;

            if (hovered) hoveredMacroIndex = i;

            if (selected) {
                context.fill(panelX + 6, y + 1, panelX + panelWidth - 6, y + macroHeight - 1, 0x33333333);
            }

            // Icon and name
            String icon = macro.isRecording() ? "[REC]" : macro.isPlaying() ? "[PLAY]" : "[ ]";
            int nameColor = macro.isPlaying() ? 0xFF6B6B : 0xEEEEEE;
            context.drawText(textRenderer, Text.literal(icon), panelX + 12, y + 4, nameColor, true);
            context.drawText(textRenderer, Text.literal(macro.getName()), panelX + 50, y + 4, nameColor, true);
            context.drawText(textRenderer, Text.literal(macro.getActions().size() + " actions"), panelX + 12, y + 16, 0x555555, true);

            // Play/Stop button
            int btnX = panelX + panelWidth - 55;
            context.fill(btnX, y + 5, btnX + 42, y + 20, macro.isPlaying() ? 0x882222 : 0x226622);
            context.drawText(textRenderer, Text.literal(macro.isPlaying() ? "STOP" : "PLAY"), btnX + 4, y + 7, 0xFFFFFF, true);
        }

        // Selected macro details
        if (selectedMacroName != null) {
            Macro macro = macros.stream().filter(m -> m.getName().equals(selectedMacroName)).findFirst().orElse(null);
            if (macro != null) {
                context.fill(panelX + 8, detailY, panelX + panelWidth - 8, panelY + panelHeight - 8, 0x44000000);
                context.drawText(textRenderer, Text.literal("Trigger: " + macro.getTriggerKey()), panelX + 14, detailY + 6, 0xAAAAAA, true);
                context.drawText(textRenderer, Text.literal("Repeat: " + macro.getRepeatMode()), panelX + 14, detailY + 18, 0xAAAAAA, true);
            }
        }
    }

    @Override
    public boolean mouseClicked(Click mouseClick, boolean used) {
        if (hoveredMacroIndex >= 0 && hoveredMacroIndex < macros.size()) {
            selectedMacroName = macros.get(hoveredMacroIndex).getName();
            NyssanClient.macroManager.saveMacros();
            return true;
        }

        if (selectedMacroName != null) {
            Macro macro = macros.stream().filter(m -> m.getName().equals(selectedMacroName)).findFirst().orElse(null);
            if (macro != null) {
                if (macro.isPlaying()) macro.stopPlaying();
                else macro.startPlaying();
                NyssanClient.macroManager.saveMacros();
                return true;
            }
        }
        return super.mouseClicked(mouseClick, used);
    }
}
