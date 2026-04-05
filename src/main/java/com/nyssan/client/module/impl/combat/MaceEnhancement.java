package com.nyssan.client.module.impl.combat;

import com.nyssan.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.math.Box;

public class MaceEnhancement extends Module {
    private float minFallDistance = 3.0f;
    private float triggerCooldown = 500; // ms
    private long lastAttack = 0;

    public MaceEnhancement() {
        super("Mace Enhancement", "Auto-hits with Mace on ground slam", Category.COMBAT);
    }

    @Override
    protected void onDisable() {
        minFallDistance = 3.0f;
        lastAttack = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // Check if holding mace
        if (mc.player.getMainHandStack().getItem() != Items.MACE
            && mc.player.getOffHandStack().getItem() != Items.MACE) return;

        // Check fall distance
        if (mc.player.fallDistance < minFallDistance) return;

        // Check cooldown
        long now = System.currentTimeMillis();
        if (now - lastAttack < triggerCooldown) return;

        // Find nearest enemy to hit at ground level
        LivingEntity target = findNearestGroundEnemy(mc);
        if (target == null) return;

        // Wait until player is close enough to ground to hit
        if (mc.player.fallDistance > 2.0f && mc.player.isOnGround()) {
            mc.getNetworkHandler().sendPacket(
                PlayerInteractEntityC2SPacket.attack(target, false)
            );
            net.minecraft.util.Hand hand = mc.player.getMainHandStack().getItem() == Items.MACE ? net.minecraft.util.Hand.MAIN_HAND : net.minecraft.util.Hand.OFF_HAND;
            mc.player.swingHand(hand);
            lastAttack = now;
        }
    }

    private LivingEntity findNearestGroundEnemy(MinecraftClient mc) {
        if (mc.world == null) return null;
        Box searchBox = mc.player.getBoundingBox().expand(4.0);
        return mc.world.getEntitiesByClass(
            PlayerEntity.class, searchBox,
            e -> e != mc.player && !e.isDead()
        ).stream()
            .filter(p -> p.isOnGround() || mc.player.fallDistance >= minFallDistance)
            .min(java.util.Comparator.comparingDouble(p -> p.distanceTo(mc.player)))
            .orElse(null);
    }

    private float getMinFallDistance() { return minFallDistance; }
}
