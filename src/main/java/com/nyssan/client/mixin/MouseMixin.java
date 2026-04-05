package com.nyssan.client.mixin;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.module.impl.movement.FreeLook;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow private double cursorDeltaX;
    @Shadow private double cursorDeltaY;

    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void onUpdateMouseHead(CallbackInfo ci) {
        FreeLook freeLook = (FreeLook) NyssanClient.moduleManager.getModules().stream()
            .filter(m -> m instanceof FreeLook).findFirst().orElse(null);
        if (freeLook != null && freeLook.isEnabled()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;
            double dx = cursorDeltaX * 0.15 * mc.options.getMouseSensitivity().getValue();
            double dy = cursorDeltaY * 0.15 * mc.options.getMouseSensitivity().getValue();
            freeLook.updateCamera((float) -dx, (float) dy);
        }
    }
}
