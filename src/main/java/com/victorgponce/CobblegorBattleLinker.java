package com.victorgponce;

import com.victorgponce.command.ModCommands;
import com.victorgponce.manager.RedisManager;
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

        try {
            RedisManager.get();
        } catch (Exception e) {
            LOGGER.error("--------------------------------------------------");
            LOGGER.error(" EL SERVIDOR NO PUEDE INICIAR SIN REDIS ");
            LOGGER.error(" Revisa tu config/CobblegorBattleLinker/config.json");
            LOGGER.error("--------------------------------------------------");
            throw e; // Relanzar para que Fabric detenga el proceso
        }

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ModCommands.register(dispatcher);
        });
    }
}