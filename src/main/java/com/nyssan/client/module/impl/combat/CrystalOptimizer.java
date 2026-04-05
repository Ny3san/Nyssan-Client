package com.nyssan.client.module.impl.combat;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Comparator;
import java.util.List;

public class CrystalOptimizer extends Module {
    private int placeDelay = 0;
    private int nextSequence = 0;
    private static final int MIN_PLACE_DELAY = 1;

    public CrystalOptimizer() {
        super("Crystal Optimizer", "Auto-places and breaks End Crystals for PvP", Category.COMBAT);
    }

    @Override
    protected void onEnable() {
        nextSequence = 0;
        placeDelay = 0;
        NyssanClient.LOGGER.info("Crystal Optimizer enabled");
    }

    @Override
    protected void onDisable() {
        placeDelay = 0;
        NyssanClient.LOGGER.info("Crystal Optimizer disabled");
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isDead()) return;

        if (placeDelay > 0) {
            placeDelay--;
            return;
        }

        // Try to break existing crystals near enemies
        Entity crystal = findDamagingCrystal(mc);
        if (crystal != null) {
            breakCrystal(crystal, mc);
            placeDelay = MIN_PLACE_DELAY;
            return;
        }

        // Place a crystal near nearest enemy if holding crystal
        if (isHoldingCrystal(mc)) {
            AbstractClientPlayerEntity target = findNearestEnemy(mc);
            if (target != null) {
                placeCrystal(target, mc);
                placeDelay = MIN_PLACE_DELAY;
            }
        }
    }

    private boolean isHoldingCrystal(MinecraftClient mc) {
        return mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL
            || mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
    }

    private AbstractClientPlayerEntity findNearestEnemy(MinecraftClient mc) {
        List<AbstractClientPlayerEntity> players = mc.world.getPlayers();
        return players.stream()
            .filter(p -> p != mc.player && !p.isDead() && distance(p, mc.player) <= 12)
            .min(Comparator.comparingDouble(p -> distance(p, mc.player)))
            .orElse(null);
    }

    private double distance(Entity a, Entity b) {
        return Math.sqrt(a.squaredDistanceTo(b.getX(), b.getY(), b.getZ()));
    }

    private Vec3d getEntityPos(Entity e) {
        return new Vec3d(e.getX(), e.getY(), e.getZ());
    }

    private void placeCrystal(AbstractClientPlayerEntity target, MinecraftClient mc) {
        Vec3d tPos = getEntityPos(target);
        Vec3d[] testPositions = {
            new Vec3d(tPos.x + 1, tPos.y, tPos.z),
            new Vec3d(tPos.x - 1, tPos.y, tPos.z),
            new Vec3d(tPos.x, tPos.y, tPos.z + 1),
            new Vec3d(tPos.x, tPos.y, tPos.z - 1),
            new Vec3d(tPos.x + 1, tPos.y + 1, tPos.z),
            new Vec3d(tPos.x - 1, tPos.y + 1, tPos.z),
            new Vec3d(tPos.x, tPos.y + 1, tPos.z + 1),
            new Vec3d(tPos.x, tPos.y + 1, tPos.z - 1),
        };

        Vec3d bestPos = null;
        for (Vec3d pos : testPositions) {
            var bPos = new net.minecraft.util.math.BlockPos(
                (int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
            if (mc.world.getBlockState(bPos.down()).isSolidBlock(mc.world, bPos.down())) {
                if (!mc.world.getBlockState(bPos).isSolidBlock(mc.world, bPos)) {
                    bestPos = pos;
                    break;
                }
            }
        }

        if (bestPos == null) return;

        // Raytrace check
        Vec3d eyePos = mc.player.getEyePos();
        if (!hasLineOfSight(mc, eyePos, bestPos)) return;

        // Place crystal
        var bPos = new net.minecraft.util.math.BlockPos(
            (int) Math.floor(bestPos.x), (int) Math.floor(bestPos.y), (int) Math.floor(bestPos.z));
        var hitPos = bestPos.subtract(0, 0.5, 0);
        BlockHitResult hitResult = new BlockHitResult(
            hitPos, Direction.UP, bPos, false);

        Hand hand = mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL
            ? Hand.MAIN_HAND : Hand.OFF_HAND;

        mc.getNetworkHandler().sendPacket(
            new PlayerInteractBlockC2SPacket(hand, hitResult, nextSequence++)
        );
    }

    private EndCrystalEntity findDamagingCrystal(MinecraftClient mc) {
        AbstractClientPlayerEntity target = findNearestEnemy(mc);
        if (target == null) return null;

        Vec3d pPos = getEntityPos(mc.player);
        List<EndCrystalEntity> crystals = mc.world.getEntitiesByClass(
            EndCrystalEntity.class,
            new Box(pPos.add(6, 6, 6), pPos.subtract(6, 6, 6)),
            e -> true);

        return crystals.stream()
            .filter(c -> distance(c, target) <= 8)
            .filter(c -> hasLineOfSight(mc, mc.player.getEyePos(), getEntityPos(c)))
            .min(Comparator.comparingDouble(c -> distance(c, target)))
            .orElse(null);
    }

    private void breakCrystal(Entity crystal, MinecraftClient mc) {
        mc.getNetworkHandler().sendPacket(
            PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking())
        );
    }

    private boolean hasLineOfSight(MinecraftClient mc, Vec3d from, Vec3d to) {
        net.minecraft.util.hit.BlockHitResult result = mc.world.raycast(
            new RaycastContext(from, to,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        return result == null || result.getPos().distanceTo(to) < 1.0;
    }
}
