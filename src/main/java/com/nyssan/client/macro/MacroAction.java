package com.nyssan.client.macro;

public class MacroAction {
    private ActionType type;
    private int keyCode;
    private int delay;
    private String label;

    public enum ActionType {
        KEY_PRESS,   // Simulate key press
        CLICK,       // Simulate mouse click
        WAIT,        // Wait for delay ms
        PLACE_BLOCK, // Place a block
        BREAK_BLOCK  // Break a block
    }

    public MacroAction(ActionType type, int keyCode, int delay, String label) {
        this.type = type;
        this.keyCode = keyCode;
        this.delay = delay;
        this.label = label;
    }

    public static MacroAction waitAction(int ms) {
        return new MacroAction(ActionType.WAIT, 0, ms, "Wait " + ms + "ms");
    }

    public static MacroAction keyPressAction(int keyCode, int delay, String label) {
        return new MacroAction(ActionType.KEY_PRESS, keyCode, delay, label);
    }

    public static MacroAction clickAction(int button, int delay, String label) {
        return new MacroAction(ActionType.CLICK, button, delay, label);
    }

    public ActionType getType() { return type; }
    public int getKeyCode() { return keyCode; }
    public int getDelay() { return delay; }
    public String getLabel() { return label; }
}
