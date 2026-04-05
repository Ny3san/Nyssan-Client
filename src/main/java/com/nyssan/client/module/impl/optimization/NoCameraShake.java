package com.nyssan.client.module.impl.optimization;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.module.Module;

public class NoCameraShake extends Module {
    public NoCameraShake() {
        super("No Camera Shake", "Remove tremor de câmera em combate", Category.OPTIMIZATION);
    }

    @Override
    protected void onEnable() {
        NyssanClient.LOGGER.info("No Camera Shake enabled - shake removed via mixin");
    }

    @Override
    protected void onDisable() {
        NyssanClient.LOGGER.info("No Camera Shake disabled");
    }
}
