package com.nyssan.client.module.impl.combat;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AnchorDefense extends Module {
    public AnchorDefense() {
        super("Anchor Defense", "Detects and defends against Anchor PvP", Category.COMBAT);
    }

    @Override
    protected void onDisable() {}

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // Respawn anchors only work in Nether
        RegistryKey<net.minecraft.world.World> dimKey = mc.world.getRegistryKey();
        boolean isNether = dimKey == net.minecraft.world.World.NETHER;
        if (!isNether) {
            // Respawn anchors don't function outside Nether
            return;
        }

        // Detect nearby respawn anchors
        int range = 8;
        BlockPos center = mc.player.getBlockPos();
        List<BlockPos> anchors = new ArrayList<>();

        for (int x = -range; x <= range; x++) {
            for (int y = -2; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (mc.world.getBlockState(pos).isOf(Blocks.RESPAWN_ANCHOR)) {
                        anchors.add(pos);
                    }
                }
            }
        }

        // If there's an anchor with charges > 0, warn
        for (BlockPos anchor : anchors) {
            int charges = mc.world.getBlockState(anchor).get(RespawnAnchorBlock.CHARGES);
            if (charges > 0) {
                NyssanClient.LOGGER.warn("Charged Anchor nearby at {}! Charges: {}", anchor, charges);

                // Check if enemies are near the anchor
                Vec3d aPos = Vec3d.ofCenter(anchor);
                Box anchorBox = new Box(aPos.add(5, 5, 5), aPos.subtract(5, 5, 5));
                List<AbstractClientPlayerEntity> nearby = mc.world.getPlayers().stream()
                    .filter(p -> p != mc.player && !p.isDead() && anchorBox.contains(
                        new Vec3d(p.getX(), p.getY(), p.getZ())))
                    .toList();

                if (!nearby.isEmpty()) {
                    NyssanClient.LOGGER.warn("Enemy near charged anchor - consider moving away!");
                }
            }
        }
    }
}
