package com.nyssan.client.mixin;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.module.impl.movement.FreeLook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class FreeLookMixin {
    @Shadow private float yaw;
    @Shadow private float pitch;

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdateEnd(net.minecraft.world.World area,
                             net.minecraft.entity.Entity entity,
                             boolean thirdPerson,
                             boolean inverseView,
                             float tickDelta,
                             CallbackInfo ci) {
        FreeLook freeLook = getModule();
        if (freeLook != null && freeLook.isEnabled() && freeLook.isCameraLocked()) {
            this.yaw = freeLook.getCameraYaw();
            this.pitch = freeLook.getCameraPitch();
        }
    }

    private FreeLook getModule() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return null;
        return (FreeLook) NyssanClient.moduleManager.getModules().stream()
            .filter(m -> m instanceof FreeLook).findFirst().orElse(null);
    }
}
