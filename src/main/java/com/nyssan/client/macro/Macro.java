package com.nyssan.client.macro;

import com.nyssan.client.config.ConfigSerializable;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

public class Macro {
    private String name;
    private int triggerKey;
    private RepeatMode repeatMode;
    private int repeatCount;
    private boolean recording;
    private boolean playing;
    private final List<MacroAction> actions = new ArrayList<>();
    private int currentActionIndex = 0;
    private long lastActionTime = 0;
    private int playCount = 0;

    public enum RepeatMode {
        WHILE_PRESSED,
        FIXED_TIMES,
        INFINITE
    }

    public Macro(String name, int triggerKey) {
        this.name = name;
        this.triggerKey = triggerKey;
        this.repeatMode = RepeatMode.WHILE_PRESSED;
        this.repeatCount = 1;
        this.recording = false;
        this.playing = false;
    }

    public void addAction(MacroAction action) {
        actions.add(action);
    }

    public void startPlaying() {
        this.playing = true;
        this.currentActionIndex = 0;
        this.lastActionTime = System.currentTimeMillis();
        this.playCount = 0;
    }

    public void stopPlaying() {
        this.playing = false;
        this.recording = false;
    }

    public void update() {
        if (!playing || actions.isEmpty()) return;

        long now = System.currentTimeMillis();
        MacroAction current = actions.get(currentActionIndex);

        if (now - lastActionTime >= current.getDelay()) {
            lastActionTime = now;
            currentActionIndex++;

            if (currentActionIndex >= actions.size()) {
                playCount++;
                if (repeatMode == RepeatMode.INFINITE || (repeatMode == RepeatMode.FIXED_TIMES && playCount < repeatCount)) {
                    currentActionIndex = 0;
                } else {
                    stopPlaying();
                }
            }
        }
    }

    public void startRecording() { this.recording = true; }
    public void stopRecording() { this.recording = false; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getTriggerKey() { return triggerKey; }
    public void setTriggerKey(int triggerKey) { this.triggerKey = triggerKey; }
    public RepeatMode getRepeatMode() { return repeatMode; }
    public void setRepeatMode(RepeatMode mode) { this.repeatMode = mode; }
    public int getRepeatCount() { return repeatCount; }
    public void setRepeatCount(int count) { this.repeatCount = count; }
    public boolean isRecording() { return recording; }
    public boolean isPlaying() { return playing; }
    public List<MacroAction> getActions() { return actions; }

    public JsonObject toConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", name);
        obj.addProperty("triggerKey", triggerKey);
        obj.addProperty("repeatMode", repeatMode.name());
        obj.addProperty("repeatCount", repeatCount);

        JsonArray actionsArray = new JsonArray();
        for (MacroAction action : actions) {
            JsonObject actionObj = new JsonObject();
            actionObj.addProperty("type", action.getType().name());
            actionObj.addProperty("keyCode", action.getKeyCode());
            actionObj.addProperty("delay", action.getDelay());
            actionObj.addProperty("label", action.getLabel());
            actionsArray.add(actionObj);
        }
        obj.add("actions", actionsArray);
        return obj;
    }

    public static Macro fromConfig(JsonObject obj) {
        String name = obj.get("name").getAsString();
        int triggerKey = obj.get("triggerKey").getAsInt();
        RepeatMode repeatMode = RepeatMode.valueOf(obj.get("repeatMode").getAsString());
        int repeatCount = obj.has("repeatCount") ? obj.get("repeatCount").getAsInt() : 1;

        Macro macro = new Macro(name, triggerKey);
        macro.setRepeatMode(repeatMode);
        macro.setRepeatCount(repeatCount);

        if (obj.has("actions")) {
            for (var elem : obj.getAsJsonArray("actions")) {
                JsonObject actionObj = elem.getAsJsonObject();
                MacroAction.ActionType type = MacroAction.ActionType.valueOf(actionObj.get("type").getAsString());
                int keyCode = actionObj.get("keyCode").getAsInt();
                int delay = actionObj.get("delay").getAsInt();
                String label = actionObj.get("label").getAsString();
                macro.addAction(new MacroAction(type, keyCode, delay, label));
            }
        }
        return macro;
    }
}
