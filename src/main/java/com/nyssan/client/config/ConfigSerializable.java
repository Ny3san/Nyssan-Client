package com.nyssan.client.config;

public interface ConfigSerializable {
    void fromConfig(com.google.gson.JsonObject config);
    com.google.gson.JsonObject toConfig();
}
