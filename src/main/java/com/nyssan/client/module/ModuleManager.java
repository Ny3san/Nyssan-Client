package com.nyssan.client.module;

import com.nyssan.client.module.impl.combat.*;
import com.nyssan.client.module.impl.render.*;
import com.nyssan.client.module.impl.optimization.*;
import com.nyssan.client.module.impl.movement.FreeLook;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public void registerDefaults() {
        // Combat
        registerModule(new CrystalOptimizer());
        registerModule(new MaceEnhancement());
        registerModule(new AnchorDefense());

        // Movement
        registerModule(new FreeLook());

        // Render
        registerModule(new HitBoxDisplay());
        registerModule(new ArmorDurabilityView());
        registerModule(new FPSView());

        // Optimization
        registerModule(new NoCameraShake());
        registerModule(new NoFog());
        registerModule(new EntityCullingHelper());
    }

    public void registerModule(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() { return modules; }

    public Optional<Module> getModule(String name) {
        return modules.stream()
            .filter(m -> m.getName().equalsIgnoreCase(name))
            .findFirst();
    }

    public Optional<Module> getModule(Class<? extends Module> clazz) {
        return modules.stream()
            .filter(m -> m.getClass() == clazz)
            .findFirst();
    }

    public List<Module> getModulesByCategory(Module.Category category) {
        return modules.stream()
            .filter(m -> m.getCategory() == category)
            .toList();
    }

    public void onTick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }
}
