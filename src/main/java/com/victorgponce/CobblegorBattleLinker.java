package com.victorgponce;

import com.victorgponce.command.ModCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblegorBattleLinker implements ModInitializer {

    public static final String MOD_ID = "cobblegorbattlelinker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Cobblegor Battle Linker...");

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ModCommands.register(dispatcher);
        });
    }
}