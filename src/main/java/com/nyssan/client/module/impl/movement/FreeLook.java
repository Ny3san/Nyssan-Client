package com.nyssan.client.module.impl.movement;

import com.nyssan.client.module.Module;
import net.minecraft.client.MinecraftClient;

public class FreeLook extends Module {
    private float savedYaw = 0f;
    private float savedPitch = 0f;
    private float currentCameraYaw = 0f;
    private float currentCameraPitch = 0f;
    private boolean cameraLocked = false;

    public FreeLook() {
        super("FreeLook", "Moves camera freely without changing player facing", Category.MOVEMENT);
        this.keyCode = org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT;
    }

    @Override
    protected void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            savedYaw = mc.player.getYaw();
            savedPitch = mc.player.getPitch();
            currentCameraYaw = savedYaw;
            currentCameraPitch = savedPitch;
        }
    }

    @Override
    protected void onDisable() {
        // Camera will snap back to player facing naturally
        cameraLocked = false;
    }

    public float getCameraYaw() {
        return currentCameraYaw;
    }

    public float getCameraPitch() {
        return currentCameraPitch;
    }

    public void updateCamera(float yawDelta, float pitchDelta) {
        currentCameraYaw += yawDelta;
        currentCameraPitch += pitchDelta;
        currentCameraPitch = Math.max(-90f, Math.min(90f, currentCameraPitch));
        cameraLocked = true;
    }

    public boolean isCameraLocked() {
        return cameraLocked;
    }
}
