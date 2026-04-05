package com.nyssan.client.module.impl.render;

import com.nyssan.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ArmorDurabilityView extends Module {
    public ArmorDurabilityView() {
        super("Armor Durability View", "Mostra durabilidade da armadura no HUD", Category.RENDER);
    }

    public void renderDurability(net.minecraft.client.gui.DrawContext context, int x, int y) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : slots) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            int maxDurability = stack.getMaxDamage();
            int currentDurability = stack.getDamage();
            int remaining = maxDurability - currentDurability;
            float pct = maxDurability > 0 ? (float) remaining / maxDurability : 1.0f;

            int color = pct > 0.5 ? 0x44FF44 : pct > 0.25 ? 0xFFFF44 : 0xFF4444;
            String text = stack.getName().getString() + " " + remaining + "/" + maxDurability;
            context.drawText(mc.textRenderer, Text.literal(text), x, y, color, true);
            y += 10;
        }
    }
}
