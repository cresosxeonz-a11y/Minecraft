package com.instantleafdecay;

import com.instantleafdecay.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstantLeafDecay implements ModInitializer {
    public static final String MOD_ID = "instantleafdecay";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static ModConfig config;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Instant Leaf Decay mod");
        
        // Load configuration
        config = ModConfig.load();
        
        LOGGER.info("Instant Leaf Decay loaded with config - Enabled: {}, Instant: {}", 
            config.enabled, config.instant);
    }
    
    public static ModConfig getConfig() {
        return config;
    }
}
