package com.nyssan.client.mixin;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.event.Events;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        NyssanClient.eventBus.post(new Events.KeyEvent(input.key(), input.scancode(), action, input.modifiers()));
    }
}
