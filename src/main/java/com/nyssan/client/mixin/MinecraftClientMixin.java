package com.nyssan.client.mixin;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.event.Events;
import com.nyssan.client.ui.NewClickScreen;
import com.nyssan.client.ui.screen.MacroManagerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    private Screen currentScreen;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(RunArgs args, CallbackInfo ci) {
        NyssanClient.LOGGER.info("NYSSAN Client initialized - mixin ready");
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        NyssanClient.eventBus.post(new Events.TickEvent(Events.TickEvent.Phase.START));

        // Check menu key
        MinecraftClient mc = MinecraftClient.getInstance();
        if (NyssanClient.openMenuKey != null && NyssanClient.openMenuKey.wasPressed()) {
            mc.setScreen(new NewClickScreen());
            return;
        }
        if (NyssanClient.openMacroKey != null && NyssanClient.openMacroKey.wasPressed()) {
            mc.setScreen(new MacroManagerScreen());
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickEnd(CallbackInfo ci) {
        NyssanClient.eventBus.post(new Events.TickEvent(Events.TickEvent.Phase.END));
        NyssanClient.moduleManager.onTick();
        NyssanClient.macroManager.update();
    }
}
