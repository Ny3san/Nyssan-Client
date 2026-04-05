package com.nyssan.client;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.ClientModInitializer;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nyssan.client.config.ConfigManager;
import com.nyssan.client.module.ModuleManager;
import com.nyssan.client.macro.MacroManager;
import com.nyssan.client.event.EventBus;
import com.nyssan.client.ui.screen.NyssanMenuScreen;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class NyssanClient implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "nyssan";
    public static final String MOD_NAME = "NYSSAN";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    // Core systems
    public static ModuleManager moduleManager;
    public static MacroManager macroManager;
    public static ConfigManager configManager;
    public static EventBus eventBus;

    // Keybinds
    public static KeyBinding openMenuKey;
    public static KeyBinding openMacroKey;

    @Override
    public void onInitialize() {
        LOGGER.info("Starting {} Client v{}", MOD_NAME, VERSION);
        LOGGER.info("Honoring the memory of Technoblade");
        LOGGER.info("\"Stand tall and fight back.\" - Technoblade");

        // Initialize managers
        eventBus = new EventBus();
        configManager = new ConfigManager();
        configManager.load();

        moduleManager = new ModuleManager();
        moduleManager.registerDefaults();

        macroManager = new MacroManager();
        macroManager.loadMacros();
    }

    @Override
    public void onInitializeClient() {
        // Shared category for all keybindings
        KeyBinding.Category nyssanCategory = KeyBinding.Category.create(Identifier.of("nyssan", "nyssan"));

        // Register keybinds
        openMenuKey = new KeyBinding(
            "key.nyssan.open_menu",
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            nyssanCategory
        );

        openMacroKey = new KeyBinding(
            "key.nyssan.open_macro",
            GLFW.GLFW_KEY_LEFT_BRACKET,
            nyssanCategory
        );
    }

    /** Check if a menu key is being pressed */
    public static boolean isMenuKeyPressed() {
        while (openMenuKey.wasPressed()) return true;
        return false;
    }
}
