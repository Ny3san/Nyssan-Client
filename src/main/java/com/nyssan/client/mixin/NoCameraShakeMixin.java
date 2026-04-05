package com.nyssan.client.mixin;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.module.impl.optimization.NoCameraShake;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class NoCameraShakeMixin {
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        NoCameraShake mod = (NoCameraShake) NyssanClient.moduleManager.getModules().stream()
            .filter(m -> m instanceof NoCameraShake).findFirst().orElse(null);
        if (mod != null && mod.isEnabled()) {
            ci.cancel();
        }
    }
}
