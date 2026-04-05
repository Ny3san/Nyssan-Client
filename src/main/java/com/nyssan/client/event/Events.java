package com.nyssan.client.event;

public class Events {

    public static class KeyEvent {
        private final int key;
        private final int scancode;
        private final int action;
        private final int modifiers;

        public KeyEvent(int key, int scancode, int action, int modifiers) {
            this.key = key;
            this.scancode = scancode;
            this.action = action;
            this.modifiers = modifiers;
        }

        public int getKey() { return key; }
        public int getScancode() { return scancode; }
        public int getAction() { return action; }
        public int getModifiers() { return modifiers; }
    }

    public static class TickEvent {
        public enum Phase { START, END }
        private final Phase phase;

        public TickEvent(Phase phase) { this.phase = phase; }
        public Phase getPhase() { return phase; }
    }

    public static class RenderEvent {
        private final float tickDelta;
        public RenderEvent(float tickDelta) { this.tickDelta = tickDelta; }
        public float getTickDelta() { return tickDelta; }
    }
}
