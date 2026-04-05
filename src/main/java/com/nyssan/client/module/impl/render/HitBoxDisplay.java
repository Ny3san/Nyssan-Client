package com.nyssan.client.module.impl.render;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class HitBoxDisplay extends Module {
    private static int tickCounter = 0;

    public HitBoxDisplay() {
        super("HitBox Display", "Exibe hitbox dos inimigos", Category.RENDER);
    }

    @Override
    protected void onEnable() {
        tickCounter = 0;
        NyssanClient.LOGGER.info("HitBox Display enabled - logging nearby entities");
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        tickCounter++;
        if (tickCounter % 100 != 0) return;

        // Log nearby entity info as a simplified hitbox display
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity && entity != mc.player && entity.isAlive()) {
                double d = mc.player.squaredDistanceTo(entity);
                if (d < 256) {
                    NyssanClient.LOGGER.debug("Entity: {} at ({},{},{}) hp:{}/{}",
                        entity.getType().getName(),
                        entity.getX(), entity.getY(), entity.getZ(),
                        ((LivingEntity) entity).getHealth(),
                        ((LivingEntity) entity).getMaxHealth());
                }
            }
        }
    }
}
