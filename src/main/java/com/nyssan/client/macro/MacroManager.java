package com.nyssan.client.macro;

import com.nyssan.client.NyssanClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MacroManager {
    private final List<Macro> macros = new ArrayList<>();
    private final Path macroPath = Path.of("config", "nyssan", "macros.json");

    public void loadMacros() {
        try {
            Files.createDirectories(macroPath.getParent());
            if (Files.exists(macroPath)) {
                JsonObject config = NyssanClient.configManager.loadConfig(macroPath);
                if (config.has("macros")) {
                    for (var elem : config.getAsJsonArray("macros")) {
                        macros.add(Macro.fromConfig(elem.getAsJsonObject()));
                    }
                }
                NyssanClient.LOGGER.info("Loaded {} macros", macros.size());
            }
        } catch (Exception e) {
            NyssanClient.LOGGER.warn("Failed to load macros: {}", e.getMessage());
        }
    }

    public void saveMacros() {
        try {
            Files.createDirectories(macroPath.getParent());
            JsonArray arr = new JsonArray();
            for (Macro macro : macros) {
                arr.add(macro.toConfig());
            }
            JsonObject config = new JsonObject();
            config.add("macros", arr);
            NyssanClient.configManager.saveConfig(macroPath, config);
            NyssanClient.LOGGER.info("Saved {} macros", macros.size());
        } catch (Exception e) {
            NyssanClient.LOGGER.warn("Failed to save macros: {}", e.getMessage());
        }
    }

    public void update() {
        for (Macro macro : macros) {
            macro.update();
        }
    }

    public void addMacro(Macro macro) { macros.add(macro); }
    public void removeMacro(Macro macro) { macros.remove(macro); }
    public List<Macro> getMacros() { return macros; }

    public Macro getMacroByKey(int key) {
        return macros.stream()
            .filter(m -> m.getTriggerKey() == key)
            .findFirst()
            .orElse(null);
    }
}
