package com.nyssan.client.module.impl.optimization;

import com.nyssan.client.NyssanClient;
import com.nyssan.client.module.Module;

public class NoFog extends Module {
    public NoFog() {
        super("No Fog", "Remove nevoeiro para melhor visão em PvP", Category.OPTIMIZATION);
    }

    @Override
    protected void onEnable() {
        NyssanClient.LOGGER.info("No Fog enabled - fog will be removed via mixin");
    }

    @Override
    protected void onDisable() {
        NyssanClient.LOGGER.info("No Fog disabled");
    }
}
