package com.nyssan.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath = Path.of("config", "nyssan", "config.json");
    private JsonObject config = new JsonObject();

    public void load() {
        try {
            Files.createDirectories(configPath.getParent());
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                config = JsonParser.parseString(json).getAsJsonObject();
            }
        } catch (Exception e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(config));
        } catch (Exception e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    public JsonObject loadConfig(Path path) throws IOException {
        String json = Files.readString(path);
        return JsonParser.parseString(json).getAsJsonObject();
    }

    public void saveConfig(Path path, JsonObject config) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(config));
    }

    public JsonObject getConfig() { return config; }

    // Module state
    public boolean isModuleEnabled(String moduleName) {
        if (config.has(moduleName)) {
            return config.getAsJsonObject(moduleName).get("enabled").getAsBoolean();
        }
        return false;
    }

    public void setModuleEnabled(String moduleName, boolean enabled) {
        JsonObject moduleConfig = new JsonObject();
        moduleConfig.addProperty("enabled", enabled);
        config.add(moduleName, moduleConfig);
    }
}
