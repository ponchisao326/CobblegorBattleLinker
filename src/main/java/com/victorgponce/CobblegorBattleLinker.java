package com.victorgponce;

import com.victorgponce.command.ModCommands;
import com.victorgponce.event.PlayerJoinListener;
import com.victorgponce.manager.RedisManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Cobblegor Battle Linker mod.
 * <p>
 * This class implements {@link ModInitializer} to handle the server-side initialization logic.
 * It is responsible for verifying the Redis connection on startup and registering the mod's commands.
 */
public class CobblegorBattleLinker implements ModInitializer {

    public static final String MOD_ID = "cobblegorbattlelinker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Initializes the mod.
     * <p>
     * This method:
     * <ul>
     * <li>Initializes the {@link RedisManager} to ensure a valid connection exists.</li>
     * <li>Aborts the server startup if Redis is unreachable to prevent data inconsistencies.</li>
     * <li>Registers the command structure via {@link ModCommands}.</li>
     * </ul>
     */
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
            throw e; // Relaunch to stop the server
        }

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ModCommands.register(dispatcher));

        // Register event listeners
        ServerPlayConnectionEvents.JOIN.register(new PlayerJoinListener());
    }
}