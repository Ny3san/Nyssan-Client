package com.nyssan.client.mixin;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.module.impl.optimization.NoFog;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class NoFogMixin {
    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private void onApplyFog(Camera camera, int fogShape, RenderTickCounter tickCounter, float renderDistanceOverride, ClientWorld world, CallbackInfoReturnable<Vector4f> cir) {
        NoFog noFog = (NoFog) NyssanClient.moduleManager.getModules().stream()
            .filter(m -> m instanceof NoFog).findFirst().orElse(null);
        if (noFog != null && noFog.isEnabled()) {
            // Return default fog color with minimal distance fog
            float fogColor = world.getDimension().ambientLight();
            cir.setReturnValue(new Vector4f(fogColor, fogColor, fogColor, 0));
            cir.cancel();
        }
    }
}
