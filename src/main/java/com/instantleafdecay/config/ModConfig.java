package com.instantleafdecay.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.instantleafdecay.InstantLeafDecay;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Paths.get("config", "instantleafdecay.json");
    
    public boolean enabled = true;
    public boolean instant = true;
    
    public static ModConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                ModConfig config = GSON.fromJson(json, ModConfig.class);
                InstantLeafDecay.LOGGER.info("Configuration loaded from {}", CONFIG_PATH);
                return config;
            } catch (IOException e) {
                InstantLeafDecay.LOGGER.error("Failed to load config, using defaults", e);
                return createDefault();
            }
        } else {
            return createDefault();
        }
    }
    
    private static ModConfig createDefault() {
        ModConfig config = new ModConfig();
        config.save();
        InstantLeafDecay.LOGGER.info("Created default configuration at {}", CONFIG_PATH);
        return config;
    }
    
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            InstantLeafDecay.LOGGER.error("Failed to save config", e);
        }
    }
}
